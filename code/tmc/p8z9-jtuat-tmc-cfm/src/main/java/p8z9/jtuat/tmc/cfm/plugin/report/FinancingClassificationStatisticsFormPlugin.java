package p8z9.jtuat.tmc.cfm.plugin.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.report.ReportList;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.sdk.plugin.Plugin;
import kd.tmc.cfm.report.helper.ReportCommonHelper;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 融资分类统计表（本年融资情况汇总表）
 * 报表界面插件
 */
public class FinancingClassificationStatisticsFormPlugin extends AbstractReportFormPlugin implements Plugin {

    @Override
    public void beforeQuery(ReportQueryParam queryParam) {
        super.beforeQuery(queryParam);
        String value_dimension = (String) this.getModel().getValue("p8z9_filter_dimension");
        String[] fields = null;
        ReportList listTable = this.getView().getControl("reportlistap");
        switch (value_dimension) {
            case "financing_model":
                fields = new String[]{"融资模式", "本位币", "融入金额（原币）", "融入金额（本位币）", "平均利率成本", "占比%", "本金（本位币）", "利息（本位币）", "合计排序"};
                break;
            case "financing_subject":
                fields = new String[]{"融资主体", "本位币", "融入金额（原币）", "融入金额（本位币）", "平均利率成本", "占比%", "本金（本位币）", "利息（本位币）", "合计排序"};
                break;
            case "credit_method":
                fields = new String[]{"担保方式", "本位币", "融入金额（原币）", "融入金额（本位币）", "占比%", "合计排序"};
                break;
        }
        ReportCommonHelper.rebuildColumn(fields, listTable);
    }

    @Override
    public void processRowData(String gridPK, DynamicObjectCollection dc, ReportQueryParam queryParam) {
        super.processRowData(gridPK, dc, queryParam);
        // 初始化融资余额总额
        BigDecimal totalBaseAmt = BigDecimal.ONE;

        // 查找sumlevel为2的融资余额（代表为合计）
        for (DynamicObject row : dc) {
            if (row.getInt("p8z9_sumlevel") == 2) {
                totalBaseAmt = row.getBigDecimal("p8z9_baseamt");
                break; // 提前终止，提高效率
            }
        }

        // 获取维度值
        String valueDimension = (String) this.getModel().getValue("p8z9_filter_dimension");

        // 判断是否需要计算占比
        if ("financing_model".equals(valueDimension) ||
                "financing_subject".equals(valueDimension) ||
                "credit_method".equals(valueDimension)) {

            boolean nonZeroTotal = totalBaseAmt.compareTo(BigDecimal.ZERO) != 0;

            for (DynamicObject dy : dc) {
                // int sumLevel = dy.getInt("p8z9_sumlevel");
                // if (sumLevel != 2 && sumLevel != 1) { // 非合计行
                BigDecimal baseAmt = dy.getBigDecimal("p8z9_baseamt");
                BigDecimal percentage = nonZeroTotal ? baseAmt.divide(totalBaseAmt, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) : BigDecimal.ZERO;
                dy.set("p8z9_percentage", percentage);
                // }
            }
        }
    }
}