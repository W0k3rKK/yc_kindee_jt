package p8z9.jtuat.tmc.cfm.plugin.form;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.math.BigDecimal;
import java.util.*;

/**
 * 单据界面插件
 * 债券还款处理 计算折本位币种汇率，及折本位币种利息金额
 */
public class RepaymentBillPlugin extends AbstractBillPlugIn implements Plugin {

    private static final String KEY_FIELD_FROMCURR = "e_currency";
    private static final String KEY_FIELD_ACTINTAMT = "e_actintamt"; //实付利息
    private static final String KEY_FIELD_TOCURR = "p8z9_tocurr";
    private static final String KEY_FIELD_EXRATETABLE = "p8z9_exratetable";
    private static final String KEY_FIELD_EXRATE = "p8z9_exrate";
    private static final String KEY_FIELD_QUOTATION = "p8z9_quotation"; //换算方式
    private static final String KEY_FIELD_FUNCTIONALAMT = "p8z9_functionalamt";
    private static final String KEY_FIELD_REPAYAMOUNT = "p8z9_repayamount"; //折本位币种本金金额
    private static final String KEY_FIELD_EREPAYAMOUNT = "e_repayamount"; //还款本金
    private static final String KEY_FIELD_BIZDATE = "bizdate";

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.getExrate();
        this.CalAmt();
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String propName = e.getProperty().getName();
        List<String> keyList = Arrays.asList("e_ispayinst", KEY_FIELD_ACTINTAMT, KEY_FIELD_FROMCURR);
        if (!keyList.contains(propName)) {
            return;
        }

        this.getExrate();
        this.CalAmt();
    }

    private void getExrate() {
        IDataModel model = this.getModel();
        DynamicObject fromCurr = (DynamicObject) model.getValue(KEY_FIELD_FROMCURR);
        DynamicObject toCurr = (DynamicObject) model.getValue(KEY_FIELD_TOCURR);
        DynamicObject exrateTable = (DynamicObject) model.getValue(KEY_FIELD_EXRATETABLE);
        String quotation = (String) model.getValue(KEY_FIELD_QUOTATION);
        Date bizdate = (Date) model.getValue(KEY_FIELD_BIZDATE);

        BigDecimal exrate = BaseDataServiceHelper.getExchangeRate(exrateTable.getLong("id"),
                fromCurr.getLong("id"), toCurr.getLong("id"), bizdate);

        model.setValue(KEY_FIELD_EXRATE, exrate);
    }

    private void CalAmt() {
        IDataModel model = this.getModel();

        BigDecimal exrate = (BigDecimal) model.getValue(KEY_FIELD_EXRATE);

        BigDecimal actintAmt = (BigDecimal) model.getValue(KEY_FIELD_ACTINTAMT);
        BigDecimal actintCurAmt = exrate.multiply(actintAmt);
        model.setValue(KEY_FIELD_FUNCTIONALAMT, actintCurAmt);

        BigDecimal repayAmt = (BigDecimal) model.getValue(KEY_FIELD_EREPAYAMOUNT);
        BigDecimal repayCurAmt = exrate.multiply(repayAmt);
        model.setValue(KEY_FIELD_REPAYAMOUNT, repayCurAmt);
    }
}