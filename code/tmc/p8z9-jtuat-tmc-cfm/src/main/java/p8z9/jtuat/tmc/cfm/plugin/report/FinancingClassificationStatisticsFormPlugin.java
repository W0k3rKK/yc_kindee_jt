package p8z9.jtuat.tmc.cfm.plugin.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.sdk.plugin.Plugin;
import kd.tmc.cfm.report.helper.ReportCommonHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 报表界面插件
 */
public class FinancingClassificationStatisticsFormPlugin extends AbstractReportFormPlugin implements Plugin {

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);

        this.getControl("reportfilterap");
        String name = e.getProperty().getName();
        if ("p8z9_filter_dimension".equals(name)) {
            String value_dimension = (String) e.getChangeSet()[0].getNewValue();
            String[] hideFields = null;
            String[] showFields = null;
            switch (value_dimension) {
                case "financing_model"://融资模式
                case "credit_method"://增信方式
                    hideFields = new String[]{"p8z9_filter_org"};
                    break;
                case "financing_subject"://融资主体
                    showFields = new String[]{"p8z9_filter_org", "p8z9_filter_finproduct", "p8z9_filter_currencies"};
                    break;
            }
            IDataModel model = this.getModel();
            if (hideFields != null) {
                for (String hideField : hideFields) {
                    model.setValue(hideField, null);
                }
                this.getView().setVisible(false, hideFields);
            }
            if (showFields != null) {
                this.getView().setVisible(true, showFields);
            }
        }
    }

    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        super.beforeQuery(queryParam);
        String value_dimension = (String) this.getModel().getValue("p8z9_filter_dimension");
        String[] fields = null;
        ReportList listTable = this.getView().getControl("reportlistap");
        switch (value_dimension) {
            case "financing_model":
                fields = new String[]{"融资模式", "本位币", "融资余额（本位币）", "平均利率成本", "占比", "本金（本位币）", "利息（本位币）"};
                break;
            case "financing_subject":
                fields = new String[]{"融资主体", "本位币", "融资余额（本位币）", "平均利率成本", "占比", "本金（本位币）", "利息（本位币）"};
                break;
            case "credit_method":
                fields = new String[]{"担保方式", "本位币", "融资余额（本位币）", "占比"};
                break;
        }
        ReportCommonHelper.rebuildColumn(fields, listTable);
    }

    @Override
    public void processRowData(String gridPK, DynamicObjectCollection dc, ReportQueryParam queryParam) {
        super.processRowData(gridPK, dc, queryParam);

        BigDecimal total_baseamt = dc.stream()
                .map(dy -> dy.getBigDecimal("p8z9_baseamt"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);// 融资余额（本位币）总额

        String value_dimension = (String) this.getModel().getValue("p8z9_filter_dimension");
        switch (value_dimension) {
            case "financing_model":
            case "financing_subject":
            case "credit_method":
                for (DynamicObject dy : dc) {
                    BigDecimal percentage = total_baseamt.compareTo(BigDecimal.ZERO) != 0
                            ? dy.getBigDecimal("p8z9_baseamt").divide(total_baseamt, 10, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    dy.set("p8z9_percentage", percentage);// 占比 = 融资余额（本位币） / 融资余额（本位币）总额

                    // BigDecimal baseInterest = dy.getBigDecimal("p8z9_baseinterest");
                    // BigDecimal basePrincipal = dy.getBigDecimal("p8z9_baseprincipal");
                    // BigDecimal interestRate = basePrincipal.compareTo(BigDecimal.ZERO) != 0
                    //         ? baseInterest.divide(basePrincipal, 10, RoundingMode.HALF_UP)
                    //         : BigDecimal.ZERO;
                    // dy.set("p8z9_avg_interestrate", interestRate);// 平均利率成本 = 利息（本位币） / 本金（本位币）
                }
                break;
        }
    }
}