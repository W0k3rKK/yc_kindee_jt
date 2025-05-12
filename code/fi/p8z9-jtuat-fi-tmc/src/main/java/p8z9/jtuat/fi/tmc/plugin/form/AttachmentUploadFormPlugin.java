package p8z9.jtuat.fi.tmc.plugin.form;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.serialization.SerializationUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.CloseCallBack;
import kd.bos.form.FormShowParameter;
import kd.bos.form.ShowType;
import kd.bos.form.events.AfterDoOperationEventArgs;
import kd.bos.form.events.ClosedCallBackEvent;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.ISVServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单据弹出额外上传附件动态表单
 */
public class AttachmentUploadFormPlugin extends AbstractBillPlugIn implements Plugin {

    private static final Log logger = LogFactory.getLog(AttachmentUploadFormPlugin.class);

    private static final String ENTITY_NUMBER = "cfm_loancontractbill";

    private String ATTACHMENT_FORM_NUMBER;

    public AttachmentUploadFormPlugin() {
        String id = ISVServiceHelper.getISVInfo().getId();
        this.ATTACHMENT_FORM_NUMBER = id + "_attachmentpanel";
    }

    @Override
    public void afterDoOperation(AfterDoOperationEventArgs args) {
        super.afterDoOperation(args);

        IDataModel model = this.getModel();
        switch (args.getOperateKey()) {
            case "uploadattachment":

                FormShowParameter showParameter = new FormShowParameter();
                // this.getView().getFormShowParameter().getFormId();
                showParameter.setFormId(ATTACHMENT_FORM_NUMBER);//单据标识
                showParameter.getOpenStyle().setShowType(ShowType.Modal);
                List<Map<String, Object>> formData = new ArrayList<>();
                Map<String, Object> data = new HashMap<>();
                data.put("entityNumber", ENTITY_NUMBER); // 实体编码
                data.put("attKey", "attachmentpanel"); // 面板标识
                // data.put("rowKey", row.getRowKey());
                // data.put("billId", row.getPrimaryKeyValue());
                // data.put("billNo", row.getBillNo());

                data.put("billId", model.getValue("id"));
                data.put("billNo", model.getValue("billno"));
                formData.add(data);
                CloseCallBack closeCallBack = new CloseCallBack(this, "uploadattachment");
                showParameter.setCloseCallBack(closeCallBack);
                showParameter.getCustomParams().put("formData", SerializationUtils.toJsonString(formData));
                this.getView().showForm(showParameter);
                break;
        }
    }

    @Override
    public void closedCallBack(ClosedCallBackEvent evt) {
        super.closedCallBack(evt);
        String actionId = evt.getActionId();
        if ("uploadattachment".equals(actionId)) {
            this.getView().updateView("attachmentpanel");
        }
    }
}