package p8z9.jtuat.tmc.cfm.plugin.report;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.util.StringUtils;
import kd.sdk.plugin.Plugin;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 融资情况明细表、本年融资情况表
 * 报表界面插件
 */
public class FinancingStatementFormPlugin extends AbstractReportFormPlugin implements Plugin {

    private static final Log logger = LogFactory.getLog(FinancingStatementFormPlugin.class);

    @Override
    public void processRowData(String gridPK, DynamicObjectCollection rowDataDC, ReportQueryParam queryParam) {
        super.processRowData(gridPK, rowDataDC, queryParam);
        for (DynamicObject rowData : rowDataDC) {
            // 1、处理类别，将类别改为一级类目
            String finName = rowData.getString("p8z9_finproduct");
            DynamicObject fin = BusinessDataServiceHelper.loadSingleFromCache("cfm_financingvarieties",
                    new QFilter("name", "=", finName).toArray());
            if (Objects.isNull(fin)) {
                logger.error("行{}未查询到名称为 {} 的融资类别详细数据！", rowDataDC.indexOf(rowData), finName);
            } else {
                String firstCategory = this.findFirstCategory(fin);
                rowData.set("p8z9_finproduct", firstCategory);
            }

            // 2、处理期限，转换为月（m）
            String termStr = rowData.getString("p8z9_term");
            if (StringUtils.isNotEmpty(termStr)) {
                int months = this.convertToMonths(termStr);
                rowData.set("p8z9_term", String.valueOf(months) + "m");
            }

        }

    }

    /**
     * 查找一类类目
     *
     * @param fin
     * @return
     */
    private String findFirstCategory(DynamicObject fin) {
        String fullName = fin.getString("fullname");
        if (fullName.contains(".")) {
            // 如果包含 ".", 取前半部分
            return fullName.split("\\.")[0];
        } else {
            // 不包含 "."，直接返回原字符串
            return fullName;
        }
    }

    /**
     * 对期限ymd进行转换为整月（如有d，即便只有1d也算作一个月）
     *
     * @param timeStr 期限字符串
     * @return
     */
    public int convertToMonths(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return 0;
        }

        int totalMonths = 0;
        // 正则表达式匹配：数字+y/m/d（不区分大小写）
        Pattern pattern = Pattern.compile("(\\d+)\\s*([ymd])", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(timeStr);

        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();

            switch (unit) {
                case "y":
                    totalMonths += value * 12; // 年转月
                    break;
                case "m":
                    totalMonths += value;      // 月直接累加
                    break;
                case "d":
                    totalMonths += 1;          // 天无论数值大小都算1个月
                    break;
            }
        }
        return totalMonths;
    }
}