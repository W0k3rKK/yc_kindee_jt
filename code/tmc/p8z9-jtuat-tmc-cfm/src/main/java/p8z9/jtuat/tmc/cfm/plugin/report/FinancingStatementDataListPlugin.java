package p8z9.jtuat.tmc.cfm.plugin.report;

import kd.bos.algo.DataSet;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.sdk.plugin.Plugin;
import kd.tmc.cfm.common.enums.LoanTypeEnum;
import kd.tmc.cfm.report.helper.ReportCommonHelper;
import kd.tmc.cfm.report.helper.TradeFinanceRptHelper;
import kd.tmc.fbp.common.util.EmptyUtil;

import java.util.*;

/**
 * 融资情况明细表
 * 报表取数插件
 */
public class FinancingStatementDataListPlugin extends AbstractReportListDataPlugin implements Plugin {
    private static final Log logger = LogFactory.getLog(FinancingStatementDataListPlugin.class);
    // 提款处理单据上部分字段编码，为了方便索引对应，部分需从其他地方取值的字段留空占位
    private static String[] FIELD_KEY_OF_LOANBILL = {"org", "textcreditor", "", "finproduct.name", "notrepayamount", "bizdate", "term", "expiredate", "p8z9_tzll", "", "", "loancontractbill.guarantee", "description"};
    // 报表列表上的所有字段,分别对应融资主体、融资机构、区域、类别、融资余额、发放时间、期限/月、还款时间、利率、融资成本-利息、实际成本率、增信方式、备注
    private static String[] FIELDS = {"p8z9_org", "p8z9_textcreditor", "p8z9_region", "p8z9_finproduct", "p8z9_notrepayamount", "p8z9_bizdate",
            "p8z9_term", "p8z9_expiredate", "p8z9_rate", "p8z9_costinterest", "p8z9_costrate", "p8z9_guarantee", "p8z9_description"};

    /**
     * 查询数据
     *
     * @param queryParam  查询参数
     * @param selectedObj 左树（表）右表是选中左树（表）对象，左树时为节点ID，左表时为选中行数据
     * @return 返回报表数据
     * @throws Throwable 抛出查询异常
     */
    @Override
    public DataSet query(ReportQueryParam queryParam, Object selectedObj) throws Throwable {
        // 转换报表过滤条件，便于处理
        Map<String, Object> paramMap = ReportCommonHelper.transQueryParam(queryParam);

        // 根据数据源查询单据（含通用过滤条件
        DataSet dataSet = this.queryLoanBillDS(queryParam, paramMap);
        dataSet.print(true);

        // 小计行构建
        DataSet dataSetSum = dataSet.copy().groupBy(new String[]{FIELDS[0]}).sum(FIELDS[4]).sum(FIELDS[9]).finish();
        // 1、列数相等 2、列间数据类型形同
        dataSetSum = dataSetSum.addField("'小计'", FIELDS[1]);//将“小计”字段添加到String字段类型中
        dataSetSum = dataSetSum.addField("'1'", "p8z9_sumlevel");
        dataSetSum = dataSetSum.addNullField(FIELDS[2], FIELDS[3], "p8z9_billno", "p8z9_srccur", "p8z9_basenotrepayamt", FIELDS[5], FIELDS[6], FIELDS[7], FIELDS[8], FIELDS[10], FIELDS[11], FIELDS[12]);
        dataSetSum = dataSetSum.select(getSelectProps());
        dataSetSum.print(true);

        // union默认不会剔除重复数据
        dataSet = dataSet.union(dataSetSum);
        dataSet = dataSet.orderBy(new String[]{FIELDS[0], "p8z9_sumlevel"});

        // 合计行构建
        DataSet dataSetTotalSum = dataSet.copy().filter("p8z9_sumlevel = '1'").groupBy(null).sum(FIELDS[4]).sum(FIELDS[9]).finish();
        dataSetTotalSum = dataSetTotalSum.addField("'合计'", FIELDS[1]);//将“合计”字段添加到String字段类型中
        dataSetTotalSum = dataSetTotalSum.addField("'2'", "p8z9_sumlevel");
        dataSetTotalSum = dataSetTotalSum.addNullField(FIELDS[0], FIELDS[2], FIELDS[3], "p8z9_billno", "p8z9_srccur", "p8z9_basenotrepayamt", FIELDS[5], FIELDS[6], FIELDS[7], FIELDS[8], FIELDS[10], FIELDS[11], FIELDS[12]);
        dataSetTotalSum = dataSetTotalSum.select(getSelectProps());
        dataSetTotalSum.print(true);

        dataSet = dataSet.union(dataSetTotalSum);

        // 如融资机构为空则归为其他
        dataSet = dataSet.updateField(FIELDS[1], "case when p8z9_textcreditor is null then '其他' else p8z9_textcreditor end");

        return dataSet;
    }


    private DataSet queryLoanBillDS(ReportQueryParam queryParam, Map<String, Object> paramMap) {

        StringBuilder selectFields = new StringBuilder();
        selectFields.append(FIELD_KEY_OF_LOANBILL[0]).append(" AS ").append(FIELDS[0]).append(", ")
                // .append(FIELD_KEY_OF_LOANBILL[1]).append(" AS ").append(FIELDS[1]).append(", ")
                .append("'区内'").append(" AS ").append(FIELDS[2]).append(", ") // 查同类型字符串 as p8z9_region
                .append(FIELD_KEY_OF_LOANBILL[3]).append(" AS ").append(FIELDS[3]).append(", ")
                .append("billno").append(" AS ").append("p8z9_billno").append(", ")
                .append("currency").append(" AS ").append("p8z9_srccur").append(", ")
                .append(FIELD_KEY_OF_LOANBILL[4]).append(" AS ").append(FIELDS[4]).append(", ")
                .append(FIELD_KEY_OF_LOANBILL[5]).append(" AS ").append(FIELDS[5]).append(", ")
                .append(FIELD_KEY_OF_LOANBILL[6]).append(" AS ").append(FIELDS[6]).append(", ")
                .append(FIELD_KEY_OF_LOANBILL[7]).append(" AS ").append(FIELDS[7]).append(", ")
                .append(FIELD_KEY_OF_LOANBILL[8]).append(" AS ").append(FIELDS[8]).append(", ")
                // .append(FIELD_KEY_OF_LOANBILL[11]).append(" AS ").append(FIELDS[11]).append(", ")//担保方式
                .append(FIELD_KEY_OF_LOANBILL[12]).append(" AS ").append(FIELDS[12]).append(", ");

        QFilter loanBillQFilter = TradeFinanceFilterHelperExt.loanBillQFilter(queryParam);
        // 查询截止日期
        loanBillQFilter.and(new QFilter("bizdate", "<=", (Date) paramMap.get("p8z9_filter_cutoffdate")));
        QFilter elFilter = loanBillQFilter.copy();
        QFilter bondQFilter = loanBillQFilter.copy();
        QFilter blQFilter = loanBillQFilter.copy();

        List<DataSet> dataDS = new ArrayList();
        String dataSource = paramMap.get("p8z9_filter_datasource").toString();

        // 债券发行
        QFilter slCredFilter;
        if (dataSource.contains(LoanTypeEnum.BOND.getValue())) {
            slCredFilter = TradeFinanceRptHelper.getLoanTypeFilter(LoanTypeEnum.BOND.getValue());
            bondQFilter.and(slCredFilter);
            QFilter bondCredFilter = TradeFinanceRptHelper.getBondCreditorFilter(paramMap);
            bondQFilter.and(bondCredFilter);
            // String zwfxSelectFields = selectFields.toString() + "investor_entry.e_investorname" + " AS " + FIELDS[1];
            String zwfxSelectFields = selectFields.toString() + "investor_entry.e_investorname AS p8z9_textcreditor, guaranteeway AS p8z9_guarantee";
            DataSet bondDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill_bond", zwfxSelectFields, bondQFilter.toArray(), null);
            dataDS.add(bondDS);
        }

        // 融资租赁
        DataSet loanBillDS;
        if (dataSource.contains("fl")) {

        }

        // 企业提款
        if (dataSource.contains("entrustloan")) {
            elFilter.and(new QFilter("loantype", "in", Arrays.asList("entrust", "ec")));
            // String qytkSelectFields = selectFields.toString() + FIELD_KEY_OF_LOANBILL[1] + " AS " + FIELDS[1];
            String qytkSelectFields = selectFields.toString() + "textcreditor AS p8z9_textcreditor, loancontractbill.guarantee AS p8z9_guarantee";
            loanBillDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill", qytkSelectFields, elFilter.toArray(), null);
            dataDS.add(loanBillDS);
        }


        // 银行提款
        if (dataSource.contains("bankloan")) {
            blQFilter.and("loantype", "=", LoanTypeEnum.BANKLOAN.getValue());
            // String yhtkSelectFields = selectFields.toString() + FIELD_KEY_OF_LOANBILL[1] + " AS " + FIELDS[1];
            String yhtkSelectFields = selectFields.toString() + "textcreditor AS p8z9_textcreditor, loancontractbill.guarantee AS p8z9_guarantee";
            loanBillDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill", yhtkSelectFields, blQFilter.toArray(), null);
            dataDS.add(loanBillDS);
        }

        // 计算金额本位币、金额公式
        DataSet dataSet = EmptyUtil.isEmpty(dataDS) ? null : (DataSet) dataDS.stream().reduce(DataSet::union).get();
        String curUnit = paramMap.get("p8z9_filter_currencyunit").toString();//货币单位
        Date cutoffDate = (Date) paramMap.get("p8z9_filter_cutoffdate");
        String curField = "p8z9_srccur";
        DataSet RateDs = TradeFinanceFilterHelperExt.getExChangeRateDs(dataSet, curField, 1L, cutoffDate);
        dataSet = dataSet.addNullField("p8z9_basenotrepayamt", "p8z9_costinterest", "p8z9_costrate");
        dataSet = dataSet.addField("'0'", "p8z9_sumlevel");
        // 连接后需查询的字段，查出汇率进行计算
        List<String> joinSelPropList = new ArrayList<>();
        Collections.addAll(joinSelPropList, this.getSelectProps());
        joinSelPropList.add("tarcurrency");
        joinSelPropList.add("rate");
        dataSet = dataSet.leftJoin(RateDs).on(curField, "tarcurrency").select(joinSelPropList.toArray(new String[0])).finish();
        dataSet = dataSet.updateField(FIELDS[4], FIELDS[4] + "/" + curUnit);//融资余额（原币）
        dataSet = dataSet.updateField("p8z9_basenotrepayamt", "p8z9_notrepayamount * rate / " + curUnit);//融资余额（本位币）
        dataSet = dataSet.updateField("p8z9_costinterest", "p8z9_basenotrepayamt * p8z9_rate /" + curUnit);//融资成本-利息=融资余额（本位币）*利率
        dataSet = dataSet.updateField("p8z9_costrate", "p8z9_costinterest / p8z9_basenotrepayamt /" + curUnit);//实际成本率=融资成本-利息/融资余额（本位币）
        dataSet = dataSet.select(this.getSelectProps());
        dataSet.print(true);

        return dataSet;
    }

    /*private QFilter getCommonFilters(ReportQueryParam queryParam, Map<String, Object> paramMap) {
        List<QFilter> filterList = new ArrayList<>();
        //  获取过滤条件
        List<FilterItemInfo> filters = queryParam.getFilter().getFilterItems();

        for (FilterItemInfo filterItem : filters) {
            switch (filterItem.getPropName()) {
                // 查询条件截止日期
                case "p8z9_filter_cutoffdate":
                    if (filterItem.getDate() != null) {
                        filterList.add(new QFilter("bizdate", "<=", filterItem.getDate()));
                    }
                    break;
                // 查询条件资金组织（借款人）
                case "p8z9_filter_org":
                    if (filterItem.getValue() != null) {
                        List<Long> orgIds = this.getQueryOrgIds(queryParam, "p8z9_filter_org");
                        QFilter loanBillQFilter = TradeFinanceFilterHelperExt.loanBillQFilter(orgIds);
                        filterList.add(loanBillQFilter);
                    }
                    break;
                // 查询条件融资品种
                case "p8z9_filter_finproduct":
                    if (filterItem.getValue() != null) {
                        filterList.add(new QFilter("finproduct.id", "in", ((DynamicObject) filterItem.getValue()).getPkValue()));
                    }
                    break;
                // 查询条件借款币别
                case "p8z9_filter_currencies":
                    if (filterItem.getValue() != null) {
                        filterList.add(new QFilter("currency.id", "in", ((DynamicObject) filterItem.getValue()).getPkValue()));
                    }
                    break;
                default:
                    break;
            }
        }

        return null;
    }*/


    /**
     * 返回查询字段
     *
     * @return
     */
    private String[] getSelectProps() {
        return new String[]{FIELDS[0], FIELDS[1], FIELDS[2], FIELDS[3], "p8z9_billno", "p8z9_srccur", FIELDS[4], "p8z9_basenotrepayamt", FIELDS[5], FIELDS[6],
                FIELDS[7], FIELDS[8], FIELDS[9], FIELDS[10], FIELDS[11], FIELDS[12], "p8z9_sumlevel"};
    }
}