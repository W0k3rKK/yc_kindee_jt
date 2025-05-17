package p8z9.jtuat.tmc.cfm.plugin.form;

import kd.bos.attachment.util.FileSecurityUtil;
import kd.bos.cache.CacheFactory;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.resource.ResManager;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.fileservice.BatchDownloadRequest;
import kd.bos.fileservice.FileService;
import kd.bos.fileservice.FileServiceFactory;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.lang.Lang;
import kd.bos.list.BillList;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.print.api.PrintTask;
import kd.bos.print.api.PrintWork;
import kd.bos.print.core.service.PrtAttach;
import kd.bos.print.service.BosPrintServiceHelper;
import kd.bos.servicehelper.AttachmentServiceHelper;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.template.orgctrl.model.PrintTemplateInfo;
import kd.bos.template.orgctrl.service.PrintTemplateServiceFactory;
import kd.bos.url.UrlService;
import kd.bos.util.StringUtils;
import kd.sdk.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import p8z9.jtuat.tmc.cfm.common.BatchDownloadFileDto;

import java.io.*;
import java.util.*;

/**
 * 银行借款合同/企业借款合同/债券发行申请
 * 一键下载所选单据中的附件分类面板的附件以及打印模板，层级包括单据编号及附件分类名
 */
public class BatchDownloadAttachTypeListPlugin extends AbstractListPlugin implements Plugin {

    private static final Log log = LogFactory.getLog(BatchDownloadAttachTypeListPlugin.class);

    // 附件分类面板标识
    private static final String FBD_ATTACHTYPE_PANEL = "fbd_attachtype_panel";

    private static final String BTN_BATCHDOWNLOAD = "p8z9_alldownload";

    public static final int ZIP_CACHE_TIME = 1800;

    public static final String FILE_TYPE = "pdf";

    private static FileService fs = FileServiceFactory.getAttachmentFileService();

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);

        this.addItemClickListeners(BTN_BATCHDOWNLOAD);
    }


    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);

        if (!BTN_BATCHDOWNLOAD.equals(evt.getItemKey())) {
            return;
        }

        ListSelectedRowCollection selectRows = this.getSelectedRows();
        if (selectRows.isEmpty()) {
            this.getView().showTipNotification(ResManager.loadKDString("请至少选择一行数据。", "BatchDownloadAttachTypeListPlugin_1", "tmc-cfm", new Object[0]));
            return;
        }

        //当前单据标识  如果当前是布局则会获取布局的编码，下面联查会报错
        // IListView listView = (IListView) this.getView();
        // String billFormId = listView.getBillFormId();

        BillList billList = this.getControl(BILLLISTID);
        String billFormId = billList.getEntityType().getName();//当前单据源标识

        // 文件信息对象 集合
        List<BatchDownloadFileDto> fileDtos = new ArrayList<>();

        for (ListSelectedRow row : selectRows) {

            // 当前行的单据主键id
            Long pkValue = (Long) row.getPrimaryKeyValue();
            // 当前行的单据编号
            String billNo = row.getBillNo();

            // 使用具有顺序特性的集合对
            Set<Long> attachIdSet = new LinkedHashSet<>();

            // 将上游、本单、下游，整合到一个map中  在目前类中暂时做冗余集合
            Map<String, HashSet<Long>> bills = new LinkedHashMap<>();

            // 本单
            HashSet<Long> thisBillId = new HashSet<>(1);
            thisBillId.add((Long) row.getPrimaryKeyValue());
            bills.put(billFormId, thisBillId);


            // 由于附件分类面板的特性，如果同一个分类有多个文件该分类除首行外其他行没有信息
            String lastRowAttTypeName = "";
            for (Map.Entry<String, HashSet<Long>> billInfo : bills.entrySet()) {
                // 附件分类

                // 根据单据标识获取单据模型
                // MainEntityType dt = MetadataServiceHelper.getDataEntityType(FBD_ATTACHTYPE_PANEL);
                // fbasedataid 存的是bd_attachment整个数据对象
                String selProps = "releasebillid,entryentity,entryentity.attachment,entryentity.attachment.name,entryentity.attachment.url,entryentity.attachment.fbasedataid,entryentity.attachtypename";

                // 用于QueryServiceHelper
                // String selProps = "releasebillid,entryentity,entryentity.attachment.*";
                // 附件分类面板 - 关联单据id
                QFilter q1 = new QFilter("releasebillid", QCP.in, billInfo.getValue());
                // QFilter q2 = new QFilter("", QCP.equals, "");
                // 附件分类面板 查询结果
                DynamicObject[] selAttTypes = BusinessDataServiceHelper.load(FBD_ATTACHTYPE_PANEL, selProps, new QFilter[]{q1});
                // DynamicObjectCollection selAttTypes = QueryServiceHelper.query(FBD_ATTACHTYPE_PANEL, selProps, new QFilter[]{q1});
                for (DynamicObject attType : selAttTypes) {
                    // 附件分类分录
                    DynamicObjectCollection entries = attType.getDynamicObjectCollection("entryentity");
                    for (DynamicObject entryRow : entries) {//每行分录
                        // 附件分类分录 - 附件字段（可能包含多个附件，多选基础资料）
                        DynamicObjectCollection attField = entryRow.getDynamicObjectCollection("attachment");
                        if (attField != null && !attField.isEmpty()) {
                            // attIdAndInfo 为多选基础资料 格式为：attachment[fid, 对象[...
                            for (DynamicObject attIdAndInfo : attField) {
                                // 记录附件文件信息
                                BatchDownloadFileDto attDto = new BatchDownloadFileDto();
                                attDto.setBillNumber(billNo);
                                String attachTypeName = entryRow.getString("attachtypename");
                                if (StringUtils.isBlank(attachTypeName)) {
                                    attachTypeName = lastRowAttTypeName;
                                } else {
                                    lastRowAttTypeName = attachTypeName;
                                }
                                attDto.setFileType(attachTypeName);// 文件/附件分类名
                                DynamicObject attInfo = attIdAndInfo.getDynamicObject("fbasedataid");
                                // attDto.setBillPkId(String.valueOf(attType.getLong("releasebillid")));
                                attDto.setBillPkId(String.valueOf(pkValue));
                                attDto.setFileName(attInfo.getString("name"));
                                // attDto.setFileSize(1L);
                                // String url = FilePathFactory.getFilePath().getRealPath(attInfo.getString("url"));
                                // attDto.setUrl(url);
                                attDto.setUrl(attInfo.getString("url"));

                                log.info("FileType: {}, FileName: {}", attDto.getFileType(), attDto.getFileName());

                                fileDtos.add(attDto);
                            }

                        }
                    }
                }

                // 获取打印模板
                long curUserId = RequestContext.get().getCurrUserId();
                PrintTemplateInfo tpl = PrintTemplateServiceFactory.getService().getPrintTemplate(curUserId, billFormId);
                String userSettingTplId = tpl.getUserSettingTplId();
                if (StringUtils.isBlank(userSettingTplId)) {
                    this.getView().showTipNotification(ResManager.loadKDString("没有设置打印模板，请在“打印设置”中进行设置。", "BatchDownloadAttachTypeListPlugin_2", "tmc-cfm", new Object[0]));
                    return;
                }

                BatchDownloadFileDto printDto = new BatchDownloadFileDto();
                printDto.setBillNumber(billNo);
                // 处理打印模板文件信息
                this.dealPrintDto(tpl, pkValue, printDto);

                log.info("FileType: {}, FileName: {}, FileUrl: {}", printDto.getFileType(), printDto.getFileName(), printDto.getUrl());
                fileDtos.add(printDto);
            }

        }
        log.info("fileDtos：{}", Arrays.toString(fileDtos.toArray()));
        this.getBatchDownloadUrl(fileDtos);


    }


    /**
     * 生成打印模板的下载URL
     *
     * @param tpl     打印模板信息对象，不可为null
     * @param pkValue 当前单据的主键值
     * @return 返回构造好的打印模板下载URL字符串
     */
    private void dealPrintDto(@NotNull PrintTemplateInfo tpl, Object pkValue, BatchDownloadFileDto printDto) {
        // 创建打印任务并设置相关参数
        PrintTask printTask = new PrintTask();
        printTask.setTplId(tpl.getUserSettingTplId()); // 设置模板ID

        ArrayList<Object> printPkValues = new ArrayList<>();
        printPkValues.add(pkValue); // 添加当前单据主键值
        printTask.setPkIds(printPkValues); // 设置打印任务的主键集合
        printTask.setFormId(tpl.getFormId()); // 设置表单ID

        ArrayList<PrintTask> taskList = new ArrayList<>();
        taskList.add(printTask); // 将打印任务添加到任务列表

        // 创建打印工作并设置相关参数
        PrintWork printWork = new PrintWork();
        printWork.setPageId(this.getView().getPageId()); // 设置页面ID
        printWork.setPrintLang(Lang.zh_CN.toString()); // 设置打印语言为中文
        printWork.setExpType(FILE_TYPE); // 设置导出类型为PDF
        printWork.setTaskList(taskList); // 设置打印任务列表

        // 执行打印操作获取附件详情
        PrtAttach prtAttach = BosPrintServiceHelper.execPrint(printWork);
        PrtAttach.AttachDetail attachDetail = prtAttach.getAttachDetail().get(0); // 获取第一个附件详情

        String filePath = attachDetail.getFilePath();
        String printTemplateUrl = UrlService.getDomainContextUrl() + "/tempfile/download.do?" + filePath;
        String formId = prtAttach.getFormId();
        String fileName = attachDetail.getFileName();

        printDto.setFileType("打印模板");
        printDto.setFileName(fileName);
        // printDto.setFileName(String.format("%s_打印模板.%s", billNo, FILE_TYPE));
        printDto.setBillPkId(String.valueOf(pkValue));

        if (!fs.exists(filePath)) {
            log.info("正在持久化打印模板");
            printTemplateUrl = AttachmentServiceHelper.saveTempToFileService(printTemplateUrl, "cfm", formId, pkValue, fileName);
            printDto.setUrl(printTemplateUrl);
            log.info("持久化后的打印模板url：{}", printTemplateUrl);
        }

    }

    private void getBatchDownloadUrl(List<BatchDownloadFileDto> fileDtos) {
        if (fileDtos == null || fileDtos.isEmpty()) {
            log.warn("集合为空");
            return;
        }

        // 构建根目录
        BatchDownloadRequest.Dir rootDir = new BatchDownloadRequest.Dir("批量下载");

        // 按照 billNumber 分组
        Map<String, List<BatchDownloadFileDto>> groupedByBillNo = new LinkedHashMap<>();
        for (BatchDownloadFileDto dto : fileDtos) {
            groupedByBillNo.computeIfAbsent(dto.getBillNumber(), k -> new ArrayList<>()).add(dto);
        }

        // 存放所有单据编号目录
        List<BatchDownloadRequest.Dir> billDirs = new ArrayList<>();

        // 构建每张单据对应的子目录及其文件
        for (Map.Entry<String, List<BatchDownloadFileDto>> entry : groupedByBillNo.entrySet()) {
            String billNo = entry.getKey();
            List<BatchDownloadFileDto> files = entry.getValue();

            // 创建单据编号目录
            BatchDownloadRequest.Dir billDir = new BatchDownloadRequest.Dir(billNo);

            // 按照 附件分类 分组
            Map<String, List<BatchDownloadFileDto>> groupedByFileType = new LinkedHashMap<>();
            for (BatchDownloadFileDto file : files) {
                groupedByFileType.computeIfAbsent(file.getFileType(), k -> new ArrayList<>()).add(file);
            }

            // 存放该单据下的所有附件分类目录
            List<BatchDownloadRequest.Dir> fileTypeDirs = new ArrayList<>();

            // 遍历每个文件分类，创建目录并添加文件
            for (Map.Entry<String, List<BatchDownloadFileDto>> fileTypeEntry : groupedByFileType.entrySet()) {
                String fileType = fileTypeEntry.getKey();
                List<BatchDownloadFileDto> filesInType = fileTypeEntry.getValue();

                // 创建附件分类目录
                BatchDownloadRequest.Dir fileTypeDir = new BatchDownloadRequest.Dir(fileType);

                // 添加文件节点
                List<BatchDownloadRequest.File> batchFiles = new ArrayList<>();
                for (BatchDownloadFileDto fileDto : filesInType) {
                    batchFiles.add(new BatchDownloadRequest.File(fileDto.getFileName(), fileDto.getUrl()));
                }

                // 设置该目录下的文件
                fileTypeDir.setFiles(batchFiles.toArray(new BatchDownloadRequest.File[0]));

                // 将该附件分类目录添加到单据目录下
                fileTypeDirs.add(fileTypeDir);
            }

            // 设置单据目录下的子目录
            billDir.setDirs(fileTypeDirs.toArray(new BatchDownloadRequest.Dir[0]));

            // 将单据目录添加到根目录下
            billDirs.add(billDir);
        }

        // 设置根目录下的子目录
        rootDir.setDirs(billDirs.toArray(new BatchDownloadRequest.Dir[0]));
        String filename = String.format("附件及打印模板_%s_%s.zip", RequestContext.get().getUserName(), System.currentTimeMillis());
        String urlResult;
        String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.93 Safari/537.36";
        OutputStream fout = null;
        InputStream fin = null;
        File tempFile = null;
        try {
            BatchDownloadRequest bdr = new BatchDownloadRequest(filename);
            bdr.setDirs(rootDir.getDirs());
            tempFile = File.createTempFile(filename, ".zip");
            fout = new FileOutputStream(tempFile);
            fs.batchDownload(bdr, fout, userAgent);
            fin = new FileInputStream(tempFile);
            urlResult = CacheFactory.getCommonCacheFactory().getTempFileCache()
                    .saveAsFullUrl(bdr.getFileName(), fin, ZIP_CACHE_TIME); // 保留30分钟

            // 打开下载url
            this.openUrl(urlResult);
        } catch (Exception e) {
            log.error(String.format("Generate Download Url err: %s", e.getMessage()), e);
        } finally {
            FileSecurityUtil.safeClose(fout);
            FileSecurityUtil.safeClose(fin);
            FileSecurityUtil.safeDeleteFile(tempFile);
        }

    }

    private void openUrl(String urlResult) {
        // 打开下载链接
        if (StringUtils.isNotEmpty(urlResult)) {
            this.getView().openUrl(urlResult);
            // this.getView().showMessage(urlResult);
        } else {
            this.getView().showTipNotification(ResManager.loadKDString("生成压缩包失败，请重试", "BatchDownloadAttachTypeListPlugin_3", "tmc-cfm"));
        }
    }


    private void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception var3) {
            }
        }

    }

}




