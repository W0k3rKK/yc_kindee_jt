package p8z9.jtuat.tmc.cfm.plugin.report;

import cn.hutool.core.date.DateUtil;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.exception.KDBizException;
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
 * 利率、实际成本率四舍五入两位小数 ----
 * 融资成本-利息 考虑百分比（除以一百）----
 * 目前债券发行中，主要债权人分录没有行数未展示在报表中 ---
 * 债券发行类型，增加债券名称（取自债券发行同名字段），并把融资机构全部置为“其他” ---
 * 提款相关类型，增加合同号列 ---
 * 企业提款融资余额、利息、实际成本率未正确带出 ---
 * </p>
 */
public class FinancingStatementDataListPlugin extends AbstractReportListDataPlugin implements Plugin {

    private static final Log logger = LogFactory.getLog(FinancingStatementDataListPlugin.class);

    private Date cutoffdate;

    private Set<Object> creditors;

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
        Map<String, Object> paramMap = ReportCommonHelper.transQueryParam(queryParam);
        initParam(paramMap);
        DataSet dataSet = null;
        try {
            dataSet = queryLoanBillDS(queryParam, paramMap);
            if (dataSet == null)
                return FinanceHelper.createEmptyDS();
            int maxLevel = getVarietyMaxLevel();
            String expr = buildCategoryExpression(maxLevel, "category");
            String selVarietyFields = "name AS finproduct.name,fullname," + expr;
            DataSet varietyDs = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_financingvarieties", selVarietyFields, (new QFilter("enable", "=", "1"))
                    .toArray(), "fullname");
            dataSet = dataSet.leftJoin(varietyDs).on("p8z9_finproduct", "finproduct.name").select(dataSet.getRowMeta().getFieldNames(), new String[]{"category"}).finish();
            dataSet = dataSet.updateField("p8z9_finproduct", "category");
            dataSet = dataSet.removeFields(new String[]{"category"});
            dataSet = addSumRows(dataSet);
            dataSet = dataSet.addField("1", "p8z9_basecur");
        } catch (Exception e) {
            logger.error(e);
        }
        return dataSet;
    }

    private DataSet addSumRows(DataSet dataSet) {
        // 小计行构建
        DataSet dataSetSum = dataSet.copy().groupBy(new String[]{"p8z9_org"}).sum("p8z9_basenotrepayamt").sum("p8z9_notrepayamt").sum("p8z9_costinterest").finish();
        // 1、列数相等 2、列间数据类型形同
        dataSetSum = dataSetSum.addField("'小计'", "p8z9_textcreditor");// 将“小计”字段添加到String字段类型中
        dataSetSum = dataSetSum.addField("'1'", "p8z9_sumlevel");
        dataSetSum = dataSetSum.addField("CASE WHEN p8z9_basenotrepayamt <> 0 AND p8z9_basenotrepayamt IS NOT NULL THEN p8z9_costinterest / p8z9_basenotrepayamt * 100 ELSE 0 END", "p8z9_costrate");
        dataSetSum = dataSetSum.addNullField(new String[]{
                "p8z9_shortname", "p8z9_region", "p8z9_finproduct", "p8z9_billno", "p8z9_contractno", "p8z9_contractname", "p8z9_srccur", "p8z9_bizdate", "p8z9_term", "p8z9_expiredate",
                "p8z9_rate", "p8z9_guarantee", "p8z9_description", "p8z9_loanbillid"});
        dataSetSum = dataSetSum.select(getSelectProps());

        // union默认不会剔除重复数据
        dataSet = dataSet.union(dataSetSum);
        dataSet = dataSet.orderBy(new String[]{"p8z9_org", "p8z9_sumlevel"});

        // 合计行构建
        DataSet dataSetTotalSum = dataSet.copy().filter("p8z9_sumlevel = '1'").groupBy(null).sum("p8z9_basenotrepayamt").sum("p8z9_notrepayamt").sum("p8z9_costinterest").finish();
        dataSetTotalSum = dataSetTotalSum.addField("'合计'", "p8z9_textcreditor");// 将“合计”字段添加到String字段类型中
        dataSetTotalSum = dataSetTotalSum.addField("'2'", "p8z9_sumlevel");
        dataSetTotalSum = dataSetTotalSum.addField("CASE WHEN p8z9_basenotrepayamt <> 0 AND p8z9_basenotrepayamt IS NOT NULL THEN p8z9_costinterest / p8z9_basenotrepayamt * 100 ELSE 0 END", "p8z9_costrate");
        dataSetTotalSum = dataSetTotalSum.addNullField(new String[]{
                "p8z9_shortname", "p8z9_org", "p8z9_region", "p8z9_finproduct", "p8z9_billno", "p8z9_contractno", "p8z9_contractname", "p8z9_srccur", "p8z9_bizdate", "p8z9_term",
                "p8z9_expiredate", "p8z9_rate", "p8z9_guarantee", "p8z9_description", "p8z9_loanbillid"});
        dataSetTotalSum = dataSetTotalSum.select(getSelectProps());

        dataSet = dataSet.union(dataSetTotalSum);

        return dataSet;
    }


    private DataSet queryLoanBillDS(ReportQueryParam queryParam, Map<String, Object> paramMap) {

        StringBuilder selectFields = new StringBuilder();
        selectFields.append("org AS p8z9_org, ")
                .append("'区内' AS p8z9_region, ")
                .append("finproduct.name AS p8z9_finproduct, ")
                .append("billno AS p8z9_billno, ")
                .append("currency AS p8z9_srccur, ")
                .append("bizdate AS p8z9_bizdate, ")
                .append("term AS p8z9_term, ")
                .append("expiredate AS p8z9_expiredate, ")
                .append("p8z9_tzll AS p8z9_rate, ")
                .append("description AS p8z9_description, ")
                .append("id AS p8z9_loanbillid, ")
                .append("drawamount AS p8z9_drawamount, ");

        QFilter loanBillQFilter = FinanceHelper.loanBillQFilter(queryParam);
        //  借款人类型不为客商、其他
        loanBillQFilter.and(new QFilter("debtortype", "not in", Arrays.asList("custom", "other")));

        Date startDate = (Date) paramMap.get("p8z9_daterange_startdate");
        Date endDate = (Date) paramMap.get("p8z9_daterange_enddate");
        if (!EmptyUtil.isEmpty(startDate) && !EmptyUtil.isEmpty(endDate)) {
            loanBillQFilter.and(new QFilter("bizdate", ">=", startDate));
            loanBillQFilter.and(new QFilter("expiredate", "<", DateUtil.offsetDay(endDate, 1)));
        }
        // 查询截止日期
        QFilter elFilter = loanBillQFilter.copy();
        QFilter bondQFilter = loanBillQFilter.copy();
        QFilter blQFilter = loanBillQFilter.copy();
        QFilter slQFilter = loanBillQFilter.copy();

        List<DataSet> dataDS = new ArrayList<>();
        String dataSource = paramMap.get("p8z9_filter_datasource").toString();

        // 债券发行
        if (dataSource.contains(LoanTypeEnum.BOND.getValue())) {
            QFilter bondCredFilter = TradeFinanceRptHelper.getBondCreditorFilter(paramMap);
            bondQFilter.and(bondCredFilter);
            String zwfxSelectFields = selectFields + "p8z9_issuemarket.name AS p8z9_textcreditor, guaranteeway AS p8z9_guarantee, contractno AS p8z9_contractno, contractname AS p8z9_contractname";
            DataSet bondDS = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_loanbill_bond", zwfxSelectFields, bondQFilter.toArray(), null);
            dataDS.add(bondDS);
            addCreditor2List(bondDS);
        }

        // 融资租赁
        if (dataSource.contains("fl")) {

        }

        // 企业提款
        if (dataSource.contains("entrustloan")) {
            elFilter.and(new QFilter("loantype", "in", Arrays.asList(new String[]{"entrust", "ec"})));
            String qytkSelectFields = selectFields + "textcreditor AS p8z9_textcreditor, loancontractbill.guarantee AS p8z9_guarantee, contractno AS p8z9_contractno, contractname AS p8z9_contractname";
            DataSet loanBillDS = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_loanbill", qytkSelectFields, elFilter.toArray(), null);
            dataDS.add(loanBillDS);
            addCreditor2List(loanBillDS);
        }


        // 银行提款
        if (dataSource.contains("bankloan")) {
            blQFilter.and("loantype", "=", LoanTypeEnum.BANKLOAN.getValue());
            String yhtkSelectFields = selectFields + "textcreditor AS p8z9_textcreditor, loancontractbill.guarantee AS p8z9_guarantee, contractno AS p8z9_contractno, contractname AS p8z9_contractname";
            DataSet loanBillDS = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_loanbill", yhtkSelectFields, blQFilter.toArray(), null);
            dataDS.add(loanBillDS);
            addCreditor2List(loanBillDS);
        }
        // 合并数据集，并且去重
        DataSet dataSet = EmptyUtil.isEmpty(dataDS) ? null : (DataSet) dataDS.stream().reduce(DataSet::union).get();

        List<Long> loanBillIds = TradeFinanceFilterHelper.getloanBillIds(dataSet, "p8z9_loanbillid");
        DataSet enotrePayAmtDS = FinanceHelper.enotrePayAmtDS(loanBillIds, this.cutoffdate, "p8z9_loanbillid", "p8z9_notrepayamt", getClass());
        dataSet = dataSet.leftJoin(enotrePayAmtDS).on("p8z9_loanbillid", "p8z9_loanbillid").select(dataSet.getRowMeta().getFieldNames(), new String[]{"p8z9_notrepayamt"}).finish();

        if (dataSource.contains("bankloan")) {
            // 银团贷款
            slQFilter.and("loantype", "=", LoanTypeEnum.BANKSLOAN.getValue());
            String slSelectFields = selectFields + "banksyndicate_entry.e_bank.name AS p8z9_textcreditor, loancontractbill.guarantee AS p8z9_guarantee, contractno AS p8z9_contractno, contractname AS p8z9_contractname, banksyndicate_entry.e_shareamount AS drawamount,banksyndicate_entry.e_bank.name AS bankname";
            DataSet slDS = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_loanbill", slSelectFields, slQFilter.toArray(), null);
            List<Long> slLoanBillIdList = FinanceHelper.getLoanBillIds(slDS, "p8z9_loanbillid");
            QFilter slRepayFilter = new QFilter("loans.e_loanbill.id", "in", slLoanBillIdList);
            slRepayFilter.and(new QFilter("bizdate", "<", DateUtil.offsetDay(this.cutoffdate, 1)));
            String slRepaySelectFields = "billno,slentryentity.s_loanbillno.id AS loanbillid, slentryentity.s_loanbillno.number AS loanbillno, slentryentity.s_bank.name AS bankname, slentryentity.s_repayamount AS repayamt";
            DataSet slRepayDS = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_repaymentbill", slRepaySelectFields, slRepayFilter.toArray(), null);
            slRepayDS = slRepayDS.groupBy(new String[]{"loanbillid", "bankname"}).sum("repayamt").finish();
            slDS = slDS.leftJoin(slRepayDS).on("p8z9_loanbillid", "loanbillid").on("bankname", "bankname").select(slDS.getRowMeta().getFieldNames(), new String[]{"repayamt"}).finish();
            slDS = slDS.addField("drawamount-repayamt", "p8z9_notrepayamt");
            slDS = slDS.updateField("p8z9_notrepayamt", "CASE WHEN p8z9_notrepayamt <> NULL THEN p8z9_notrepayamt ELSE drawamount END");
            slDS = slDS.select(dataSet.getRowMeta().getFieldNames());
            dataSet = dataSet.union(slDS).orderBy(new String[]{"p8z9_org"});
        }

        DataSet finOrgDS = QueryServiceHelper.queryDataSet(getClass().getName(), "bd_finorginfo", "name,bank_cate.name AS bank_name", (new QFilter("name", "in", this.creditors)).toArray(), null);
        dataSet = dataSet.leftJoin(finOrgDS).on("p8z9_textcreditor", "name").select(dataSet.getRowMeta().getFieldNames(), new String[]{"bank_name AS p8z9_shortname"}).finish();
        dataSet = dataSet.updateField("p8z9_textcreditor", "CASE WHEN p8z9_textcreditor IS NULL THEN '其他' ELSE p8z9_textcreditor END");
        dataSet = dataSet.updateField("p8z9_shortname", "CASE WHEN p8z9_shortname <> NULL THEN p8z9_shortname ELSE p8z9_textcreditor END");
        dataSet = this.calAmt(dataSet, paramMap);
        dataSet = dataSet.updateField("p8z9_term", "(Year(p8z9_expiredate)-Year(p8z9_bizdate))*12 + (Month(p8z9_expiredate)-Month(p8z9_bizdate)) + (CASE WHEN Day(p8z9_expiredate)-Day(p8z9_bizdate)>0 THEN 1 ELSE 0 END)");

        return dataSet;
    }

    private DataSet calAmt(DataSet dataSet, Map<String, Object> paramMap) {
        // 单据id集合
        // List<Long> loanBillIds = TradeFinanceFilterHelper.getloanBillIds(dataSet, "p8z9_loanbillid");
        // 融资余额（原币） 取时点的未还本金汇总
        // DataSet enotrePayAmtDS = FinanceHelper.enotrePayAmtDS(loanBillIds, cutoffdate, "p8z9_loanbillid", "p8z9_notrepayamt", this.getClass());
        // dataSet = dataSet.leftJoin(enotrePayAmtDS).on("p8z9_loanbillid", "p8z9_loanbillid").select(dataSet.getRowMeta().getFieldNames(), new String[]{"p8z9_notrepayamt"}).finish();

        // 计算金额本位币、金额公式
        String curUnit = paramMap.get("p8z9_filter_currencyunit").toString();// 货币单位
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
        dataSet = dataSet.updateField("p8z9_notrepayamt", "p8z9_notrepayamt / " + curUnit);// 融资余额（原币）
        dataSet = dataSet.updateField("p8z9_basenotrepayamt", "p8z9_notrepayamt * rate");// 融资余额（本位币）
        dataSet = dataSet.updateField("p8z9_costinterest", "p8z9_basenotrepayamt * p8z9_rate / 100");// 融资成本-利息=融资余额（本位币）*利率/100
        dataSet = dataSet.updateField("p8z9_costrate", "CASE WHEN p8z9_basenotrepayamt <> 0 AND p8z9_basenotrepayamt IS NOT NULL THEN p8z9_costinterest / p8z9_basenotrepayamt * 100 ELSE 0 END");// 实际成本率=融资成本-利息/融资余额（本位币）
        dataSet = dataSet.select(this.getSelectProps());

        return dataSet;
    }

    /**
     * 返回查询字段
     *
     * @return
     */
    private String[] getSelectProps() {
        return new String[]{
                "p8z9_org", "p8z9_textcreditor", "p8z9_shortname", "p8z9_region", "p8z9_finproduct", "p8z9_billno", "p8z9_contractno", "p8z9_contractname", "p8z9_srccur", "p8z9_notrepayamt",
                "p8z9_basenotrepayamt", "p8z9_bizdate", "p8z9_term", "p8z9_expiredate", "p8z9_rate", "p8z9_costinterest", "p8z9_costrate", "p8z9_guarantee", "p8z9_description", "p8z9_sumlevel",
                "p8z9_loanbillid"};
    }

    private void addCreditor2List(DataSet loanBillDS) {
        String field = "p8z9_textcreditor";
        if (this.creditors == null)
            this.creditors = new LinkedHashSet();
        List<Object> newData = FinanceHelper.getFieldFromDS(loanBillDS, field);
        if (!newData.isEmpty())
            this.creditors.addAll(newData);
    }

    private String buildCategoryExpression(int maxLevel, String aliasName) {
        StringBuilder sb = new StringBuilder("CASE ");
        for (int i = 1; i <= maxLevel; i++) {
            sb.append("WHEN level = ").append(i).append(" THEN ");
            sb.append(getFullNameFieldByLevel(i));
            sb.append(" ");
        }
        sb.append("ELSE '其他' END AS " + aliasName);
        return sb.toString();
    }

    private String getFullNameFieldByLevel(int level) {
        StringBuilder path = new StringBuilder("fullname");
        for (int i = 1; i < level; i++)
            path.insert(0, "parent.");
        return path.toString();
    }

    private int getVarietyMaxLevel() {
        DynamicObjectCollection dc = QueryServiceHelper.query(getClass().getName(), "cfm_financingvarieties", "level", null, "level desc", 1);
        if (!dc.isEmpty()) {
            return (dc.get(0)).getInt("level");
        }
        logger.error("查询融资品种最大级次失败");
        throw new KDBizException("查询融资品种最大级次失败");
    }
}