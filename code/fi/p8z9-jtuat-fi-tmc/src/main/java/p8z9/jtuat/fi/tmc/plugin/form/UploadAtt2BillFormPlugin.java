package p8z9.jtuat.fi.tmc.plugin.form;

import kd.bos.cache.CacheFactory;
import kd.bos.cache.TempFileCache;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.metadata.dynamicobject.DynamicObjectType;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.fileservice.FileItem;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.*;
import kd.bos.form.control.AttachmentPanel;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.BeforeClosedEvent;
import kd.bos.form.events.MessageBoxClosedEvent;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.ORM;
import kd.bos.orm.util.CollectionUtils;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.ISVServiceHelper;
import kd.bos.servicehelper.attachment.AttachmentFieldServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import kd.bos.util.FileNameUtils;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;


/**
 * 表单：上传附件到单据
 * 编码：****_uploadatt2bills
 */
public class UploadAtt2BillFormPlugin extends AbstractFormPlugin {

    private static final Log logger = LogFactory.getLog(UploadAtt2BillFormPlugin.class);

    public String ATTACH_KEY; // 动态表单的附件面板标识 - 根据情况修改

    public UploadAtt2BillFormPlugin() {
        String id = ISVServiceHelper.getISVInfo().getId();
        this.ATTACH_KEY = id + "_attachmentpanelap";
    }

    @Override
    public void beforeClosed(BeforeClosedEvent e) {
        super.beforeClosed(e);
        AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
        List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
        if (attachmentData.size() > 0) {
            e.setCancel(true);
            this.getView().showConfirm(ResManager.LoadKDString("是否放弃已上传的附件？", "UploadAtt2BillFormPlugin_0"),
                    MessageBoxOptions.YesNo, new ConfirmCallBackListener("cancel_callback", this));
        }
    }

    @Override
    public void confirmCallBack(MessageBoxClosedEvent evt) {
        switch (evt.getCallBackId()) {
            case "cancel_callback":
                if (MessageBoxResult.Yes.equals(evt.getResult())) {
                    this.cleanAllAttach();
                    this.getView().close();
                }
                break;
            case "clean_callback":
                if (MessageBoxResult.Yes.equals(evt.getResult())) {
                    this.cleanAllAttach();
                }
                break;
        }

    }

    private void cleanAllAttach() {
        AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
        List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
        for (Map<String, Object> data : attachmentData) {
            attachmentPanel.remove(data);
        }
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        if (this.hasAttachmentUploading()) {
            this.getView().showTipNotification(ResManager.LoadKDString("附件上传中，请稍后再试。", "UploadAtt2BillFormPlugin_1"));
            return;
        }
        String operateKey = args.getOperateKey();
        if ("upload".equals(operateKey)) {
            AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
            List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
            boolean hasTimeoutAtt = false;
            StringBuilder timeoutMessage = new StringBuilder(ResManager.LoadKDString("临时附件已超时，请重新上传以下文件：\r\n", "UploadAtt2BillFormPlugin_2"));
            TempFileCache tempFileCache = CacheFactory.getCommonCacheFactory().getTempFileCache();
            for (Map<String, Object> attach : attachmentData) {
                String tempUrl = (String) attach.get("url");
                if (StringUtils.isNotBlank(tempUrl) && tempUrl.contains("tempfile/download.do")) {
                    if (!tempFileCache.exists(tempUrl)) {
                        hasTimeoutAtt = true;
                        timeoutMessage.append(attach.get("name")).append("\r\n");
                    }
                }
            }
            if (hasTimeoutAtt) {
                this.getView().showConfirm(timeoutMessage.toString(), MessageBoxOptions.OK);
                return;
            }
            this.doUpload();
        } else if ("clean".equals(operateKey)) {
            AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
            List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
            if (attachmentData.size() > 0) {
                this.getView().showConfirm(ResManager.LoadKDString("是否清空当前附件面板？", "UploadAtt2BillFormPlugin_3"),
                        MessageBoxOptions.YesNo, new ConfirmCallBackListener("clean_callback", this));
            } else {
                this.getView().showTipNotification(ResManager.LoadKDString("附件面板已清空！", "UploadAtt2BillFormPlugin_4"));
            }
        }
    }

    private boolean hasAttachmentUploading() {
        IPageCache cache = this.getView().getService(IPageCache.class);
        String uploadingAttJson = cache.get("UploadingAtt" + this.getView().getPageId());
        return StringUtils.isNotBlank(uploadingAttJson);
    }

    private void doUpload() {
        AttachmentPanel attachmentPanel = getControl(ATTACH_KEY);
        List<Map<String, Object>> attachmentData = attachmentPanel.getAttachmentData();
        if (attachmentData.size() == 0) {
            this.getView().showTipNotification(ResManager.LoadKDString("请先上传文件。", "UploadAtt2BillFormPlugin_8"));
            return;
        }

        FormShowParameter showParameter = this.getView().getFormShowParameter();
        Map<String, Object> params = showParameter.getCustomParams();
        String formData = (String) params.get("formData");
        if (StringUtils.isBlank(formData)) {
            // todo: 表单直接预览
            this.getView().showTipNotification("formData is blank !");
            return;
        }
        List<Map<String, Object>> formDataList = SerializationUtils.fromJsonString(formData, List.class);
        List<Map<String, Object>> formDataErr = new ArrayList<>();
        boolean hasAttachmentDataUploadedErr = false, hasFormDataErr = false;

        ORM orm = ORM.create();
        List<DynamicObject> attBillRelList = new ArrayList<>();
        List<Map<String, Object>> attachmentDataUploaded = new ArrayList<>();
        for (Map<String, Object> attachment : attachmentData) {
            // 临时文件持久化
            String path = this.uploadFileServer(attachment);
            if (StringUtils.isBlank(path)) {
                hasAttachmentDataUploadedErr = true;
                continue;
            }
            Map<String, Object> map = new HashMap<>(attachment);
            map.put("url", path);
            attachmentDataUploaded.add(map);
            attBillRelList.clear();
            // 保存所有单据附件关联，必要：entityNumber，billId，attKey
            for (Map<String, Object> data : formDataList) {
                String rowKey = null, entityNumber = null, billNo = null, attKey = null, billId = null;
                try {
                    entityNumber = (String) data.get("entityNumber"); // 单据实体编码
                    // rowKey = String.valueOf(data.get("rowKey")); // 列表行序号
                    billId = data.get("billId") + ""; // 单据主键
                    billNo = (String) data.get("billNo"); // 单据编码
                    attKey = (String) data.get("attKey"); // 单据面板标识，eg."attachmentpanel"
                    attBillRelList.addAll(this.genAttachmentRel(entityNumber, billId, attKey, attachmentDataUploaded, orm));
                } catch (Exception e) {
                    hasFormDataErr = true;
                    // logger.error(String.format("genAttachmentRel[rowKey:%s, entityNumber:%s, billId:%s, billNo:%s, attKey:%s], err: %s",
                    //         rowKey, entityNumber, billId, billNo, attKey, e.getMessage()));

                    logger.error(String.format("genAttachmentRel[ entityNumber:%s, billId:%s, billNo:%s, attKey:%s], err: %s",
                            entityNumber, billId, billNo, attKey, e.getMessage()));
                    formDataErr.add(data);
                }
            }
            if (formDataErr.isEmpty()) {
                // 删除面板附件及临时文件
                attachmentPanel.remove(attachment);
            } else {
                List<Object> billNos = formDataErr.stream().map(e -> e.get("billNo")).collect(Collectors.toList());
                logger.error(String.format("附件[%s]，%s条单据绑定附件数据失败：\r\n%s", attachment, billNos.size(), billNos));
                formDataErr.clear();
            }
            attachmentDataUploaded.clear();
            SaveServiceHelper.save(attBillRelList.toArray(new DynamicObject[0]));
        }

        if (!hasAttachmentDataUploadedErr && !hasFormDataErr) {
            this.getView().showSuccessNotification(ResManager.LoadKDString("上传成功！", "UploadAtt2BillFormPlugin_5"));
        } else if (hasAttachmentDataUploadedErr && !hasFormDataErr) {
            this.getView().showTipNotification(ResManager.LoadKDString("以下附件上传失败，请重新上传文件！", "UploadAtt2BillFormPlugin_6"));
        } else {
            this.getView().showErrorNotification(ResManager.LoadKDString("附件上传出现未知异常，请联系管理员查询日志分析！", "UploadAtt2BillFormPlugin_7"));
        }
    }

    /**
     * 保存临时文件到文件服务器进行持久化
     *
     * @param attDataItem 已上传的附件临时文件Map信息
     * @return 上传文件服务器返回url
     * @see AttachmentServiceHelper#saveTempToFileService
     */
    private String uploadFileServer(Map<String, Object> attDataItem) {
        try {
            FileService fs = FileServiceFactory.getAttachmentFileService();
            RequestContext requestContext = RequestContext.get();
            TempFileCache fileCache = CacheFactory.getCommonCacheFactory().getTempFileCache();
            String filename = (String) attDataItem.get("name");
            String tempUrl = (String) attDataItem.get("url");
            String uuid = UUID.randomUUID().toString().replace("-", "");
            String filepath = FileNameUtils.getAttachmentFileName( // 此处 attachmentpanel 用于文件路径的唯一标识
                    requestContext.getTenantId(), requestContext.getAccountId(), "attachmentpanel", uuid + "/" + filename);
            InputStream inputStream = fileCache.getInputStream(tempUrl);
            FileItem item = new FileItem(filename, filepath, inputStream);
            String[] splits = filename.trim().split("\\.");
            String fileType = splits[splits.length - 1];
            long compressPicSize = 0L;
            int fileSize = inputStream.available() / 1024;
            if (AttachmentServiceHelper.IMAGE_TYPE.contains(fileType.toLowerCase())) {
                compressPicSize = AttachmentServiceHelper.getCompressPicSize();
            }
            String path;
            if (compressPicSize != 0L && fileSize > compressPicSize) {
                path = fs.compressPicUpload(item, compressPicSize);
            } else {
                path = fs.upload(item);
            }
            inputStream.close();
            return path;
        } catch (Exception e) {
            logger.error("uploadFileServer err: " + e.getMessage());
            return null;
        }
    }

    /**
     * 绑定附件到单据的附件面板
     *
     * @param entityNumber 实体编码
     * @param billPkId     单据主键
     * @param attList      附件信息
     * @param orm          orm实例
     * @return 附件面板实体数据，用于入库记录
     * @see AttachmentServiceHelper#upload
     */
    private DynamicObjectCollection genAttachmentRel(String entityNumber, String billPkId, String attachKey, List<Map<String, Object>> attList, ORM orm) {
        DynamicObjectType entityType = (DynamicObjectType) orm.getDataEntityType("bos_attachment");
        DynamicObjectCollection dynColl = new DynamicObjectCollection(entityType, null);
        if (attList == null || attList.size() == 0) {
            return dynColl;
        }
        long[] ids = orm.genLongIds(entityType, attList.size());
        Date today = new Date();
        for (int i = 0; i < attList.size(); i++) {
            Map<String, Object> attach = attList.get(i);
            DynamicObject dynamicObject = new DynamicObject(entityType);
            dynamicObject.set("id", ids[i]);
            dynamicObject.set("FNUMBER", attach.get("uid"));
            dynamicObject.set("FBillType", entityNumber);
            dynamicObject.set("FInterID", billPkId);
            Object lastModified = attach.get("lastModified");
            if (lastModified instanceof Date) {
                dynamicObject.set("FModifyTime", lastModified);
            } else if (lastModified instanceof Long) {
                dynamicObject.set("FModifyTime", new Date((Long) lastModified));
            } else {
                dynamicObject.set("FModifyTime", today);
            }
            dynamicObject.set("fcreatetime", attach.getOrDefault("uploadTime", today));
            String name = (String) attach.get("name");
            dynamicObject.set("FaliasFileName", name);
            dynamicObject.set("FAttachmentName", name);
            String extName = name != null ? name.substring(name.lastIndexOf(46) + 1) : "";
            dynamicObject.set("FExtName", extName);
            long compressPicSize = AttachmentServiceHelper.getCompressPicSize();
            if (AttachmentServiceHelper.IMAGE_TYPE.contains(extName.toLowerCase()) && compressPicSize != 0L && Long.parseLong(attach.get("size").toString()) > compressPicSize * 1024L) {
                dynamicObject.set("FATTACHMENTSIZE", compressPicSize * 1024L);
            } else {
                dynamicObject.set("FATTACHMENTSIZE", attach.get("size"));
            }
            dynamicObject.set("FFileId", attach.get("url"));
            dynamicObject.set("FCREATEMEN", RequestContext.get().getCurrUserId());
            dynamicObject.set("fattachmentpanel", attachKey);
            dynamicObject.set("filesource", attach.get("filesource"));
            if (attach.containsKey("description")) {
                dynamicObject.set("fdescription", attach.get("description"));
            }
            dynColl.add(dynamicObject);
        }

        return dynColl;
    }

}
