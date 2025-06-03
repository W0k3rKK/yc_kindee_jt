package p8z9.jtuat.tmc.cfm.plugin.form;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.fi.ai.util.BigdecimalUtil;
import kd.fi.cal.common.helper.ExchangeRateHelper;
import kd.sdk.plugin.Plugin;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.EventObject;
import java.util.List;
import java.util.Objects;

/**
 * 单据界面插件
 * 债券付息批量处理 根据折本位币种汇率计算折本位币种利息金额
 */
public class InterestBillPlugin extends AbstractBillPlugIn implements Plugin {

    private static final String KEY_FIELD_EXRATE = "p8z9_exrate";
    private static final String KEY_FIELD_ACTUALINSTAMT = "actualinstamt";
    private static final String KEY_FIELD_FUNCTIONALAMT = "p8z9_functionalamt";

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.CalAmt();
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        List<String> fields = Arrays.asList(KEY_FIELD_EXRATE, KEY_FIELD_ACTUALINSTAMT);
        String perName = e.getProperty().getName();
        if (fields.contains(perName)) {
            this.CalAmt();
        }
    }

    private void CalAmt() {
        IDataModel model = this.getModel();
        BigDecimal exrate = (BigDecimal) model.getValue(KEY_FIELD_EXRATE);
        BigDecimal actAmt = (BigDecimal) model.getValue(KEY_FIELD_ACTUALINSTAMT);
        if (exrate != null && actAmt != null) {
            BigDecimal amt = exrate.multiply(actAmt);
            model.setValue(KEY_FIELD_FUNCTIONALAMT, amt);
        }
    }
}