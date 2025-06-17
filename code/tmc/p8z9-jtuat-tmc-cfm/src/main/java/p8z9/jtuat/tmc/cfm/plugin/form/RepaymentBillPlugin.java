package p8z9.jtuat.tmc.cfm.plugin.form;

import kd.bos.bill.AbstractBillPlugIn;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
import kd.sdk.plugin.Plugin;

import java.math.BigDecimal;
import java.util.*;

/**
 * 单据界面插件
 * 债券还款处理 计算折本位币种汇率，及折本位币种利息金额
 */
public class RepaymentBillPlugin extends AbstractBillPlugIn implements Plugin {

    private static final Log logger = LogFactory.getLog(RepaymentBillPlugin.class);

    // 定义常量字段名称，便于维护和调用
    private static final String KEY_FIELD_FROMCURR = "e_currency";
    private static final String KEY_FIELD_ACTINTAMT = "e_actintamt"; // 实付利息
    private static final String KEY_FIELD_TOCURR = "p8z9_tocurr";
    private static final String KEY_FIELD_EXRATETABLE = "p8z9_exratetable";
    private static final String KEY_FIELD_EXRATE = "p8z9_exrate";
    private static final String KEY_FIELD_QUOTATION = "p8z9_quotation"; // 换算方式
    private static final String KEY_FIELD_FUNCTIONALAMT = "p8z9_functionalamt";
    private static final String KEY_FIELD_REPAYAMOUNT = "p8z9_repayamount"; // 折本位币种本金金额
    private static final String KEY_FIELD_EREPAYAMOUNT = "e_repayamount"; // 还款本金
    private static final String KEY_FIELD_BIZDATE = "bizdate";

    /**
     * 数据绑定完成后触发的方法
     * 调用 getExrate 和 CalAmt 方法完成汇率获取和金额计算
     */
    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.getExrate(); // 获取汇率
        this.CalAmt();    // 计算金额
    }

    /**
     * 属性值改变时触发的方法
     * 根据属性名判断是否需要重新计算汇率和金额
     */
    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String propName = e.getProperty().getName();
        List<String> keyList = Arrays.asList("e_ispayinst", KEY_FIELD_ACTINTAMT, KEY_FIELD_FROMCURR);
        if (!keyList.contains(propName)) {
            return; // 如果不是关键字段变化，则直接返回
        }

        this.getExrate(); // 重新获取汇率
        this.CalAmt();    // 重新计算金额
    }

    /**
     * 获取汇率并设置到模型中
     * 检查汇率计算所需字段是否为空，并记录详细日志
     */
    private void getExrate() {
        IDataModel model = this.getModel();
        DynamicObject fromCurr = (DynamicObject) model.getValue(KEY_FIELD_FROMCURR);
        DynamicObject toCurr = (DynamicObject) model.getValue(KEY_FIELD_TOCURR);
        DynamicObject exrateTable = (DynamicObject) model.getValue(KEY_FIELD_EXRATETABLE);
        Date bizdate = (Date) model.getValue(KEY_FIELD_BIZDATE);

        // 检查汇率计算所需字段是否为空，并记录详细日志
        if (Objects.isNull(fromCurr) || Objects.isNull(toCurr) || Objects.isNull(exrateTable)) {
            logger.info("汇率计算所需字段有空值，fromCurr：{}，toCurr：{}，exrateTable：{}", 
            fromCurr != null ? fromCurr.getLong("id") : "null", 
            toCurr != null ? toCurr.getLong("id") : "null", 
            exrateTable != null ? exrateTable.getLong("id") : "null");
            return;
        }

        BigDecimal exrate = BaseDataServiceHelper.getExchangeRate(
        exrateTable.getLong("id"), 
        fromCurr.getLong("id"), 
        toCurr.getLong("id"), 
        bizdate);

        model.setValue(KEY_FIELD_EXRATE, exrate); // 将汇率设置到模型中
    }

    /**
     * 计算金额并设置到模型中
     * 包括实付利息折本位币种金额和折本位币种本金金额的计算
     */
    private void CalAmt() {
        IDataModel model = this.getModel();

        BigDecimal exrate = (BigDecimal) model.getValue(KEY_FIELD_EXRATE);
        if (Objects.isNull(exrate)) {
            logger.info("CalAmt 汇率字段为空");
            return;
        }

        // 计算实付利息折本位币种金额
        calculateAndSetAmount(model, KEY_FIELD_ACTINTAMT, KEY_FIELD_FUNCTIONALAMT, exrate);

        // 计算折本位币种本金金额
        calculateAndSetAmount(model, KEY_FIELD_EREPAYAMOUNT, KEY_FIELD_REPAYAMOUNT, exrate);
    }

    /**
     * 提取公共逻辑为私有方法
     * 根据源字段值和汇率计算目标金额，并设置到模型中
     *
     * @param model      数据模型
     * @param sourceField 源字段名称
     * @param targetField 目标字段名称
     * @param exrate      汇率
     */
    private void calculateAndSetAmount(IDataModel model, String sourceField, String targetField, BigDecimal exrate) {
        BigDecimal amount = (BigDecimal) model.getValue(sourceField);
        if (Objects.isNull(amount)) {
            logger.warn("字段 {} 的值为空，无法进行计算", sourceField);
            return;
        }

        BigDecimal convertedAmount = exrate.multiply(amount); // 计算转换后的金额
        model.setValue(targetField, convertedAmount);         // 设置到目标字段
    }
}