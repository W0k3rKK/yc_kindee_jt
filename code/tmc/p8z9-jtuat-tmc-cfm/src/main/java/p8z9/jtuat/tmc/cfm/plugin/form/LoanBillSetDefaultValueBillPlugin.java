package p8z9.jtuat.tmc.cfm.plugin.form;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.form.FormShowParameter;
import kd.sdk.plugin.Plugin;
import p8z9.jtuat.tmc.cfm.plugin.other.ContractBondSubmitOpExt;

import java.util.EventObject;
import java.util.Map;

/**
 * 债券发行申请
 * 如通过债券注册额度列表打开单据，则根据自定义参数录入字段值
 *
 * @see OpenLoanBillListPlugin 参阅该文件去除必录显示
 *
 * @see ContractBondSubmitOpExt 搭配提交去除期限(ymd)、结束日期必录限制
 */
public class LoanBillSetDefaultValueBillPlugin extends AbstractBillPlugIn implements Plugin {

    // 通过债券注册额度列表打开单据携带的字段
    private static final String FIELD_KEY = "p8z9_approvalno";
    // 债券注册额度列表字段
    private static final String SRC_FIELD_KEY = "p8z9_textfield";


    /**
     * 新增时生效
     * @param e
     */
    @Override
    public void afterCreateNewData(EventObject e) {
        super.afterCreateNewData(e);

        Map<String, Object> customParams = this.getView().getFormShowParameter().getCustomParams();
        if (customParams.containsKey(SRC_FIELD_KEY)) {
            this.getModel().setValue(FIELD_KEY, customParams.get(OpenLoanBillListPlugin.FIELD_KEY));
        }
    }

}