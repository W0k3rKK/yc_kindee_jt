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
import kd.tmc.cfm.report.helper.TradeFinanceFilterHelper;
import kd.tmc.cfm.report.helper.TradeFinanceRptHelper;
import kd.tmc.fbp.common.util.EmptyUtil;
import p8z9.jtuat.tmc.cfm.plugin.report.util.FinanceHelper;

import java.util.*;

/**
 * 融资情况明细表（有息债务表）
 * 报表取数插件
 * <p>
 *  利率、实际成本率四舍五入两位小数 ----
 *  融资成本-利息 考虑百分比（除以一百）----
 *  目前债券发行中，主要债权人分录没有行数未展示在报表中 ---
 *  债券发行类型，增加债券名称（取自债券发行同名字段），并把融资机构全部置为“其他” ---
 *  提款相关类型，增加合同号列 ---
 *  企业提款融资余额、利息、实际成本率未正确带出 ---
 *  </p>
 */
public class FinancingStatementDataListPlugin extends AbstractReportListDataPlugin implements Plugin {

    private static final Log logger = LogFactory.getLog(FinancingStatementDataListPlugin.class);

    // 提款处理单据上部分字段编码，为了方便索引对应，部分需从其他地方取值的字段留空占位
    private static String[] FIELD_KEY_OF_LOANBILL = {"org", "textcreditor", "", "finproduct.name", "notrepayamount", "bizdate", "term", "expiredate", "p8z9_tzll", "", "", "loancontractbill.guarantee", "description"};
    // 报表列表上的所有字段,分别对应融资主体、融资机构、区域、类别、融资余额、发放时间、期限/月、还款时间、利率、融资成本-利息、实际成本率、增信方式、备注
    private static String[] FIELDS = {"p8z9_org", "p8z9_textcreditor", "p8z9_region", "p8z9_finproduct", "p8z9_notrepayamt", "p8z9_bizdate",
            "p8z9_term", "p8z9_expiredate", "p8z9_rate", "p8z9_costinterest", "p8z9_costrate", "p8z9_guarantee", "p8z9_description"};

    private Date cutoffdate;

    private void initParam(Map<String, Object> paramMap) {
        this.cutoffdate = (Date) paramMap.get("p8z9_filter_cutoffdate");
    }

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

        // 初始化成员变量
        this.initParam(paramMap);
        DataSet dataSet = null;
        try {
            // 根据数据源查询单据（含通用过滤条件
            dataSet = this.queryLoanBillDS(queryParam, paramMap);
            if (dataSet == null) {
                return FinanceHelper.createEmptyDS();
            }

            dataSet = this.addSumRows(dataSet);

            // 如融资机构为空则归为其他
            dataSet = dataSet.updateField(FIELDS[1], "CASE WHEN p8z9_textcreditor IS NULL THEN '其他' ELSE p8z9_textcreditor END");
            // 本位币 人民币
            dataSet = dataSet.addField("1", "p8z9_basecur");

        } catch (Exception e) {
            logger.error(e);
        }
        return dataSet;
    }

    private DataSet addSumRows(DataSet dataSet) {
        // 小计行构建
        DataSet dataSetSum = dataSet.copy().groupBy(new String[]{FIELDS[0]}).sum("p8z9_basenotrepayamt").sum("p8z9_notrepayamt").sum("p8z9_costinterest").finish();
        // 1、列数相等 2、列间数据类型形同
        dataSetSum = dataSetSum.addField("'小计'", FIELDS[1]);//将“小计”字段添加到String字段类型中
        dataSetSum = dataSetSum.addField("'1'", "p8z9_sumlevel");
        dataSetSum = dataSetSum.addNullField(FIELDS[2], FIELDS[3], "p8z9_billno", "p8z9_contractno", "p8z9_contractname", "p8z9_srccur", FIELDS[5], FIELDS[6], FIELDS[7], FIELDS[8], FIELDS[10], FIELDS[11], FIELDS[12], "p8z9_loanbillid");
        dataSetSum = dataSetSum.select(getSelectProps());

        // union默认不会剔除重复数据
        dataSet = dataSet.union(dataSetSum);
        dataSet = dataSet.orderBy(new String[]{FIELDS[0], "p8z9_sumlevel"});

        // 合计行构建
        DataSet dataSetTotalSum = dataSet.copy().filter("p8z9_sumlevel = '1'").groupBy(null).sum("p8z9_basenotrepayamt").sum("p8z9_notrepayamt").sum("p8z9_costinterest").finish();
        dataSetTotalSum = dataSetTotalSum.addField("'合计'", FIELDS[1]);//将“合计”字段添加到String字段类型中
        dataSetTotalSum = dataSetTotalSum.addField("'2'", "p8z9_sumlevel");
        dataSetTotalSum = dataSetTotalSum.addNullField(FIELDS[0], FIELDS[2], FIELDS[3], "p8z9_billno", "p8z9_contractno", "p8z9_contractname", "p8z9_srccur", FIELDS[5], FIELDS[6], FIELDS[7], FIELDS[8], FIELDS[10], FIELDS[11], FIELDS[12], "p8z9_loanbillid");
        dataSetTotalSum = dataSetTotalSum.select(getSelectProps());

        dataSet = dataSet.union(dataSetTotalSum);

        return dataSet;
    }


    private DataSet queryLoanBillDS(ReportQueryParam queryParam, Map<String, Object> paramMap) {

        StringBuilder selectFields = new StringBuilder();
        selectFields.append(FIELD_KEY_OF_LOANBILL[0]).append(" AS ").append(FIELDS[0]).append(", ")
                .append("'区内'").append(" AS ").append(FIELDS[2]).append(", ") // 查同类型字符串 as p8z9_region
                .append(FIELD_KEY_OF_LOANBILL[3]).append(" AS ").append(FIELDS[3]).append(", ")
                .append("billno").append(" AS ").append("p8z9_billno").append(", ")
                .append("currency").append(" AS ").append("p8z9_srccur").append(", ")
                .append(FIELD_KEY_OF_LOANBILL[5]).append(" AS ").append(FIELDS[5]).append(", ")
                .append(FIELD_KEY_OF_LOANBILL[6]).append(" AS ").append(FIELDS[6]).append(", ")
                .append(FIELD_KEY_OF_LOANBILL[7]).append(" AS ").append(FIELDS[7]).append(", ")
                .append(FIELD_KEY_OF_LOANBILL[8]).append(" AS ").append(FIELDS[8]).append(", ")
                .append(FIELD_KEY_OF_LOANBILL[12]).append(" AS ").append(FIELDS[12]).append(", ")
                .append("id AS p8z9_loanbillid, ");

        QFilter loanBillQFilter = FinanceHelper.loanBillQFilter(queryParam);
        // 查询截止日期
        // loanBillQFilter.and(new QFilter("bizdate", "<=", (Date) paramMap.get("p8z9_filter_cutoffdate")));
        QFilter elFilter = loanBillQFilter.copy();
        QFilter bondQFilter = loanBillQFilter.copy();
        QFilter blQFilter = loanBillQFilter.copy();

        List<DataSet> dataDS = new ArrayList<>();
        String dataSource = paramMap.get("p8z9_filter_datasource").toString();

        // 债券发行
        if (dataSource.contains(LoanTypeEnum.BOND.getValue())) {
            //QFilter slCredFilter = TradeFinanceRptHelper.getLoanTypeFilter(LoanTypeEnum.BOND.getValue());
            // bondQFilter.and(slCredFilter);
            QFilter bondCredFilter = TradeFinanceRptHelper.getBondCreditorFilter(paramMap);
            bondQFilter.and(bondCredFilter);
            String zwfxSelectFields = selectFields + "'其他' AS p8z9_textcreditor, guaranteeway AS p8z9_guarantee, '' AS p8z9_contractno, contractname AS p8z9_contractname";
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
            String qytkSelectFields = selectFields + "textcreditor AS p8z9_textcreditor, loancontractbill.guarantee AS p8z9_guarantee, contractno AS p8z9_contractno, '' AS p8z9_contractname";
            loanBillDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill", qytkSelectFields, elFilter.toArray(), null);
            dataDS.add(loanBillDS);
        }


        // 银行提款
        if (dataSource.contains("bankloan")) {
            blQFilter.and("loantype", "=", LoanTypeEnum.BANKLOAN.getValue());
            String yhtkSelectFields = selectFields + "textcreditor AS p8z9_textcreditor, loancontractbill.guarantee AS p8z9_guarantee, contractno AS p8z9_contractno, '' AS p8z9_contractname";
            loanBillDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill", yhtkSelectFields, blQFilter.toArray(), null);
            dataDS.add(loanBillDS);
        }
        // 合并数据集，并且去重
        DataSet dataSet = EmptyUtil.isEmpty(dataDS) ? null : (DataSet) dataDS.stream().reduce(DataSet::union).get();

        dataSet = this.calAmt(dataSet, paramMap);

        return dataSet;
    }

    private DataSet calAmt(DataSet dataSet, Map<String, Object> paramMap) {
        // 单据id集合
        List<Long> loanBillIds = TradeFinanceFilterHelper.getloanBillIds(dataSet, "p8z9_loanbillid");
        // 融资余额（原币） 取时点的未还本金汇总
        DataSet enotrePayAmtDS = FinanceHelper.enotrePayAmtDS(loanBillIds, cutoffdate, "p8z9_loanbillid", "p8z9_notrepayamt", this.getClass());
        dataSet = dataSet.leftJoin(enotrePayAmtDS).on("p8z9_loanbillid", "p8z9_loanbillid").select(dataSet.getRowMeta().getFieldNames(), new String[]{"p8z9_notrepayamt"}).finish();

        // 计算金额本位币、金额公式
        String curUnit = paramMap.get("p8z9_filter_currencyunit").toString();//货币单位
        Date cutoffDate = (Date) paramMap.get("p8z9_filter_cutoffdate");
        String curField = "p8z9_srccur";
        DataSet RateDs = FinanceHelper.getExChangeRateDs(dataSet, curField, 1L, cutoffDate);
        dataSet = dataSet.addNullField("p8z9_basenotrepayamt", "p8z9_costinterest", "p8z9_costrate");
        dataSet = dataSet.addField("'0'", "p8z9_sumlevel");
        // 连接后需查询的字段，查出汇率进行计算
        List<String> joinSelPropList = new ArrayList<>();
        Collections.addAll(joinSelPropList, this.getSelectProps());
        joinSelPropList.add("tarcurrency");
        joinSelPropList.add("rate");
        dataSet = dataSet.leftJoin(RateDs).on(curField, "tarcurrency").select(joinSelPropList.toArray(new String[0])).finish();
        dataSet = dataSet.updateField(FIELDS[4], FIELDS[4] + "/" + curUnit);//融资余额（原币）
        dataSet = dataSet.updateField("p8z9_basenotrepayamt", "p8z9_notrepayamt * rate");//融资余额（本位币）
        dataSet = dataSet.updateField("p8z9_costinterest", "p8z9_basenotrepayamt * p8z9_rate / 100");//融资成本-利息=融资余额（本位币）*利率/100
        dataSet = dataSet.updateField("p8z9_costrate", "CASE WHEN p8z9_basenotrepayamt <> 0 AND p8z9_basenotrepayamt IS NOT NULL THEN p8z9_costinterest / p8z9_basenotrepayamt * 100 ELSE 0 END");//实际成本率=融资成本-利息/融资余额（本位币）
        dataSet = dataSet.select(this.getSelectProps());

        return dataSet;
    }

    /**
     * 返回查询字段
     *
     * @return
     */
    private String[] getSelectProps() {
        return new String[]{FIELDS[0], FIELDS[1], FIELDS[2], FIELDS[3], "p8z9_billno", "p8z9_contractno", "p8z9_contractname", "p8z9_srccur", FIELDS[4], "p8z9_basenotrepayamt", FIELDS[5], FIELDS[6],
                FIELDS[7], FIELDS[8], FIELDS[9], FIELDS[10], FIELDS[11], FIELDS[12], "p8z9_sumlevel", "p8z9_loanbillid"};
    }
}