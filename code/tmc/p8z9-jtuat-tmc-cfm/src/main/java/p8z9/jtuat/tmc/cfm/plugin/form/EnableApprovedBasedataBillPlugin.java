package p8z9.jtuat.tmc.cfm.plugin.form;

import com.kingdee.util.StringUtils;
import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.form.field.BasedataEdit;
import kd.bos.form.field.events.BeforeF7SelectEvent;
import kd.bos.form.field.events.BeforeF7SelectListener;
import kd.bos.list.ListShowParameter;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 解除 银行借款合同-业务信息 中 登记合同-资产包 只能选择已审核数据的限制
 */
public class EnableApprovedBasedataBillPlugin extends AbstractBillPlugIn implements Plugin, BeforeF7SelectListener {

    private static final String BASEDATA_FIELD_KEY = "p8z9_assetpackage";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        BasedataEdit fieldEdit = this.getView().getControl(BASEDATA_FIELD_KEY);//基础资料字段标识
        fieldEdit.addBeforeF7SelectListener(this);

    }


    @Override
    public void beforeF7Select(BeforeF7SelectEvent event) {
        String fieldKey = event.getProperty().getName();
        if (StringUtils.equals(fieldKey, BASEDATA_FIELD_KEY)){
            ListShowParameter showParameter = (ListShowParameter)event.getFormShowParameter();
            //是否展示审核的改为false
            showParameter.setShowApproved(false);
        }

    }
}