package p8z9.jtuat.tmc.cfm.plugin.report;

import cn.hutool.core.date.DateUtil;
import kd.bos.algo.DataSet;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.exception.KDBizException;
import kd.bos.eye.api.log.KDException;
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
 * 融资分类统计表（本年融资情况汇总表）
 * 报表取数插件
 * <p>
 * 过滤条件查询年份明细至年月日  ---
 * 占比四舍五入保留四位小数    ---
 * 利息需除以一百，因为字段为百分比 ---
 * 按融资主体维度，金控资产管理有限公司不存在本金却存在利息（本位币）   ---
 * 平均利率成本应该为小数 ---
 * 融资余额、平均利率成本、本金未正确带出 ---
 * </p>
 */
public class FinancingClassificationStatisticsDataListPlugin extends AbstractReportListDataPlugin implements Plugin {

    private static final Log logger = LogFactory.getLog(FinancingClassificationStatisticsDataListPlugin.class);

    // 查询年份
    private Date cutoffdate;
    // 统计维度
    private String value_dimension;

    private ReportQueryParam queryParam;
    // 报表过滤条件
    private Map<String, Object> paramMap;

    private void initParam(ReportQueryParam queryParam) {
        this.paramMap = ReportCommonHelper.transQueryParam(queryParam);
        this.queryParam = queryParam;
        this.cutoffdate = (Date) this.paramMap.get("p8z9_filter_cutoffdate");
        this.value_dimension = (String) this.paramMap.get("p8z9_filter_dimension");
    }

    @Override
    public DataSet query(ReportQueryParam queryParam, Object selectedObj) throws Throwable {
        // 初始化成员变量
        this.initParam(queryParam);

        // 根据不同统计维度，进行不同数据查询及组装
        DataSet dataSet = null;
        try {
            List<String> sumFields;
            DataSet DS;
            String groupByField;
            switch (value_dimension) {
                case "financing_model":
                case "financing_subject":
                    if ("financing_model".equals(value_dimension)) {
                        // 融资模式
                        // 根据融资模式（融资品种一级级次）进行分组并汇总金额
                        groupByField = "p8z9_rzms";
                    } else {
                        // 融资主体
                        // 根据融资主体进行分组并汇总金额
                        groupByField = "p8z9_org";
                    }
                    // 获取进行初步处理包含基础字段数据
                    DS = this.getModelDs(queryParam);
                    // 按维度小计
                    dataSet = DS.copy().groupBy(new String[]{groupByField})
                            .sum("p8z9_amt")
                            .sum("p8z9_baseamt")// 融资余额（本位币）汇总
                            .sum("p8z9_baseprincipal")// 本金（本位币）汇总
                            .sum("p8z9_baseinterest")// 利息（本位币）汇总
                            .finish();
                    dataSet = dataSet.addField("1", "p8z9_basecur");// 本位币赋值
                    dataSet = dataSet.addField("'0'", "p8z9_sumlevel");// 合计排序
                    // 需要合计字段
                    sumFields = new ArrayList<>(Arrays.asList("p8z9_amt", "p8z9_baseamt", "p8z9_baseprincipal", "p8z9_baseinterest"));
                    // 融资模式、融资主体合计行
                    dataSet = this.addSumRow(dataSet, sumFields);
                    dataSet = dataSet.addField("CASE WHEN p8z9_baseprincipal <> 0 AND p8z9_baseprincipal IS NOT NULL THEN p8z9_baseinterest/p8z9_baseprincipal*100 ELSE 0 END", "p8z9_avg_interestrate");// 平均利率成本 = 利息（本位币） / 本金（本位币）

                    break;
                case "credit_method":
                    // 增信方式
                    // 根据担保方式进行分组并汇总金额
                    dataSet = this.getMethodDs();
                    dataSet = dataSet.addField("'0'", "p8z9_sumlevel");// 合计排序

                    // 需要合计字段
                    sumFields = new ArrayList<>(Arrays.asList("p8z9_amt", "p8z9_baseamt"));
                    // 增信方式合计行
                    dataSet = this.addSumRow(dataSet, sumFields);
                    break;
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return dataSet;
    }

    private DataSet addSumRow(DataSet dataSet, List<String> sumFields) {
        DataSet totalSumDS = FinanceHelper.buildSummaryDS(dataSet, sumFields, null);
        totalSumDS = totalSumDS.updateField("p8z9_sumlevel", "'2'");
        DataSet afterSumDS = dataSet.union(totalSumDS);

        return afterSumDS;
    }

    private DataSet getMethodDs() {

        // 查询数据
        DataSet methodDS = this.queryMethodDS();

        // 处理金额等数据
        // methodDS = this.calMethod(methodDS);
        methodDS = methodDS.addField("'1'", "p8z9_basecur");// 本位币

        return methodDS;
    }

    private DataSet queryMethodDS() {
        QFilter loanBillQFilter = FinanceHelper.loanBillQFilter(this.queryParam);
        String dataSource = (String) this.paramMap.get("p8z9_filter_datasource");
        String curUnit = this.paramMap.get("p8z9_filter_currencyunit").toString();
        List<String> dataSourceList = new ArrayList<>();
        if (dataSource.contains("bankloan")) {
            dataSourceList.add("loan");
            dataSourceList.add("sl");
        }
        if (dataSource.contains("entrustloan")) {
            dataSourceList.add("entrust");
            dataSourceList.add("ec");
        }
        if (dataSource.contains("bond")) {
            dataSourceList.add("bond");
        }
        if (dataSourceList.size() > 0) {
            loanBillQFilter.and("loantype", "in", dataSourceList);
        }

        // 本年初
        loanBillQFilter.and(new QFilter("bizdate", ">=", DateUtil.beginOfYear(this.cutoffdate)));

        List<Long> ids = new ArrayList<>();
        List<DataSet> dataSets = new ArrayList<>();
        // 通用sql查询字段
        String commonSelProps = "id,billno,bizdate,currency,drawamount,loancontractbill.guarantee AS guarantee";

        /**
         * <p>虽然系统上在借款合同的担保方式可多选，但在业务上借款合同不会存在担保方式只有“抵押”和“质押“的情况，如果多选一定会包含保证</p>
         * <p>所以分两种情况处理：1、担保方式多选归类为“保证”。2、担保方式单选按本身。</p>
         * <p>另外，借款合同的担保方式选了什么，关联的提款处理只能关联相应担保方式的担保合同，即如借款合同只选了“抵押“，则提款处理只能关联“抵押”的担保合同。</p>
         * <p>目前担保方式为“其他”也归类为保证</p>
         * <p>如果是抵押或质押的情况，可能单行比例或多行相加不等于百分百。那就把多行进行比例相加，如果比例>100%则取100%如果<=100%则按相加结果计算</p>
         */
        // ---1)担保方式包含'保证'(可能不止有'保证'),或为'其他'
        QFilter ensureFilter = (new QFilter("loancontractbill.guarantee", "like", "%2%")).or(new QFilter("loancontractbill.guarantee", "like", "%6%"));
        DataSet ensureDs = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_loanbill", commonSelProps, new QFilter[]{loanBillQFilter, ensureFilter}, null);
        List<Long> ensureIds = FinanceHelper.getLoanBillIds(ensureDs, "id");
        logger.info("ensureIds: {}", ensureIds);
        ids.addAll(ensureIds);
        ensureDs = ensureDs.addField("'2'", "guaranteeway")
                .addField("drawamount", "p8z9_amt");
        dataSets.add(ensureDs);
        String[] fields = ensureDs.getRowMeta().getFieldNames();

        // 由于担保方式为 2)抵押 或 3)质押，归类为本身，所以不用计算担保比例

        // ---2)担保方式为'抵押'
        QFilter mortgageFilter = new QFilter("loancontractbill.guarantee", "=", ",4,");
        DataSet mortgageDs = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_loanbill", commonSelProps, new QFilter[]{loanBillQFilter, mortgageFilter}, null);
        // logger.info(FinanceHelper.print("init mortgageDs", mortgageDs, true));
        List<Long> mortgageIds = FinanceHelper.getLoanBillIds(mortgageDs, "id");
        logger.info("mortgageIds: {}", mortgageIds);
        ids.addAll(mortgageIds);
        /*DataSet mortgageGDs = queryGContractDs(mortgageIds).groupBy(new String[]{"gsrcbillno", "gsrcbillid", "guaranteeway"}).sum("gratio").finish().updateField("gratio", "CASE WHEN gratio>100 THEN 100 ELSE gratio END");
        mortgageDs = mortgageDs.leftJoin(mortgageGDs).on("id", "gsrcbillid")
                .select(mortgageDs.getRowMeta().getFieldNames(), new String[]{"gratio"}).finish()
                .addField("drawamount*gratio/100", "p8z9_amt")
                .addField("'4'", "guaranteeway").select(fields);*/
        mortgageDs = mortgageDs.addField("'4'", "guaranteeway")
                .addField("drawamount", "p8z9_amt");
        dataSets.add(mortgageDs);

        // ---3)担保方式为'质押'
        QFilter pledgeFilter = new QFilter("loancontractbill.guarantee", "=", ",5,");
        DataSet pledgeDs = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_loanbill", commonSelProps, new QFilter[]{loanBillQFilter, pledgeFilter}, null);
        // logger.info(FinanceHelper.print("init pledgeDs", pledgeDs, true));
        List<Long> pledgeIds = FinanceHelper.getLoanBillIds(pledgeDs, "id");
        logger.info("pledgeIds: {}", pledgeIds);
        ids.addAll(pledgeIds);
        /*DataSet pledgeGDs = queryGContractDs(pledgeIds).groupBy(new String[]{"gsrcbillno", "gsrcbillid", "guaranteeway"}).sum("gratio").finish().updateField("gratio", "CASE WHEN gratio>100 THEN 100 ELSE gratio END");
        pledgeDs = pledgeDs.leftJoin(pledgeGDs).on("id", "gsrcbillid")
                .select(pledgeDs.getRowMeta().getFieldNames(), new String[]{"gratio"}).finish()
                .addField("drawamount*gratio/100", "p8z9_amt")
                .addField("'5'", "guaranteeway").select(fields);*/
        pledgeDs = pledgeDs.addField("'5'", "guaranteeway")
                .addField("drawamount", "p8z9_amt");
        dataSets.add(pledgeDs);

        // ---4)担保方式为'信用'
        QFilter creditFilter = new QFilter("loancontractbill.guarantee", "like", "%7%");
        DataSet creditDs = QueryServiceHelper.queryDataSet(getClass().getName(), "cfm_loanbill", commonSelProps, new QFilter[]{loanBillQFilter, creditFilter}, null);
        List<Long> creditIds = FinanceHelper.getLoanBillIds(creditDs, "id");
        logger.info("creditIds: {}", creditIds);
        ids.addAll(creditIds);
        creditDs = creditDs.addField("'7'", "guaranteeway").addField("drawamount", "p8z9_amt");
        dataSets.add(creditDs);

        DataSet methodDS = EmptyUtil.isEmpty(dataSets) ? null : dataSets.stream().reduce(DataSet::union).get();
        DataSet rateDs = FinanceHelper.getExChangeRateDs(methodDS, "currency", Long.valueOf(1L), this.cutoffdate);
        methodDS = methodDS.leftJoin(rateDs).on("currency", "tarcurrency").select(methodDS.getRowMeta().getFieldNames(), new String[]{"rate"}).finish().updateField("p8z9_amt", "p8z9_amt/" + curUnit).addField("p8z9_amt*rate", "p8z9_baseamt").addField("guaranteeway", "p8z9_guarantee");
        methodDS = methodDS.groupBy(new String[]{"guaranteeway"}).sum("p8z9_amt").sum("p8z9_baseamt").finish();
        methodDS = methodDS.select("guaranteeway AS p8z9_guarantee,p8z9_amt,p8z9_baseamt");
        return methodDS;
    }

    private DataSet getModelDs(ReportQueryParam queryParam) {
        // 获取融资品种最大层级
        int maxLevel = this.getVarietyMaxLevel();
        // 构建表达式
        String expr = this.buildCategoryExpression(maxLevel, "category");

        String selVarietyFields = "id AS finproduct.id,fullname," + expr;
        // 查询融资品种，并获取一级级次品种名
        DataSet varietyDs = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_financingvarieties",
                selVarietyFields, new QFilter("enable", "=", "1").toArray(), "fullname");

        // 查询提款等单据
        DataSet modelDS = this.queryModelDS(queryParam);
        // 处理金额等数据
        modelDS = this.calModelAmt(modelDS);
        // 将融资品种一级级次名，加入modelDS
        modelDS = modelDS.join(varietyDs).on("finproduct.id", "finproduct.id").select(modelDS.getRowMeta().getFieldNames(), new String[]{"category AS p8z9_rzms"}).finish();

        return modelDS;
    }

    private DataSet calModelAmt(DataSet modelDS) {

        // 单据id集合 年度
        // List<Long> yearModelLoanBillIds = TradeFinanceFilterHelper.getloanBillIds(modelDS, "loanbillid");
        // 本金（原币） 取时点的未还本金汇总，年度
        // DataSet yearPayAmtDS = FinanceHelper.enotrePayAmtDS(yearModelLoanBillIds, this.cutoffdate, "loanbillid", "p8z9_amt", this.getClass());
        // 将本金（原币）join进主DataSet
        // modelDS = modelDS.join(yearPayAmtDS).on("loanbillid", "loanbillid").select(modelDS.getRowMeta().getFieldNames(), new String[]{"p8z9_amt"}).finish();

        // 计算金额本位币、金额公式
        String curUnit = paramMap.get("p8z9_filter_currencyunit").toString();// 货币单位
        String curField = "p8z9_srccur";
        DataSet rateDs = FinanceHelper.getExChangeRateDs(modelDS, curField, 1L, this.cutoffdate);
        modelDS = modelDS.join(rateDs).on("p8z9_srccur", "tarcurrency").select(modelDS.getRowMeta().getFieldNames(), new String[]{"rate"}).finish();
        modelDS = modelDS.addField("drawamount/" + curUnit, "p8z9_amt");// 融资余额（原币）
        modelDS = modelDS.addField("p8z9_amt * rate", "p8z9_baseamt");// 融资余额（本位币）
        modelDS = modelDS.addField("p8z9_baseamt", "p8z9_baseprincipal");// 本金（本位币） 目前等于融资余额（本位币）
        modelDS = modelDS.addField("drawamount * p8z9_tzll * rate / 100 /" + curUnit, "p8z9_baseinterest");// 利息（本位币）

        return modelDS;
    }

    private DataSet queryModelDS(ReportQueryParam queryParam) {
        QFilter loanBillQFilter = FinanceHelper.loanBillQFilter(queryParam);
        // 本年初
        loanBillQFilter.and(new QFilter("bizdate", ">=", DateUtil.beginOfYear(this.cutoffdate)));
        QFilter elFilter = loanBillQFilter.copy();
        QFilter bondQFilter = loanBillQFilter.copy();
        QFilter blQFilter = loanBillQFilter.copy();

        List<DataSet> dataDSList = new ArrayList<>();
        String dataSource = paramMap.get("p8z9_filter_datasource").toString();

        // 债券发行
        // QFilter slCredFilter;
        if (dataSource.contains(LoanTypeEnum.BOND.getValue())) {
            // slCredFilter = TradeFinanceRptHelper.getLoanTypeFilter(LoanTypeEnum.BOND.getValue());
            // bondQFilter.and(slCredFilter);
            QFilter bondCredFilter = TradeFinanceRptHelper.getBondCreditorFilter(paramMap);
            bondQFilter.and(bondCredFilter);
            String zwfxSelectFields = this.getModelSelectFields(LoanTypeEnum.BOND.getValue());
            DataSet bondDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill_bond", zwfxSelectFields, bondQFilter.toArray(), null);
            dataDSList.add(bondDS);
        }

        // 融资租赁
        DataSet loanBillDS;
        if (dataSource.contains("fl")) {

        }

        // 企业提款
        if (dataSource.contains("entrustloan")) {
            elFilter.and(new QFilter("loantype", "in", Arrays.asList("entrust", "ec")));
            String qytkSelectFields = this.getModelSelectFields("entrustloan");
            loanBillDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill", qytkSelectFields, elFilter.toArray(), null);
            dataDSList.add(loanBillDS);
        }

        // 银行提款
        if (dataSource.contains("bankloan")) {
            // 普通贷款、银团贷款
            blQFilter.and("loantype", "in", Arrays.asList(LoanTypeEnum.BANKLOAN.getValue(), LoanTypeEnum.BANKSLOAN.getValue()));
            String yhtkSelectFields = this.getModelSelectFields("bankloan");
            loanBillDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill", yhtkSelectFields, blQFilter.toArray(), null);
            dataDSList.add(loanBillDS);
        }

        DataSet modelDS = EmptyUtil.isEmpty(dataDSList) ? null : dataDSList.stream().reduce(DataSet::union).get();

        return modelDS;
    }

    private String getModelSelectFields(String loanType) {
        switch (loanType) {
            case "bond":
                // return "id AS loanbillid,org AS p8z9_org,bizdate,currency AS p8z9_srccur,drawamount,p8z9_tzll,finproduct.id";
            case "entrustloan":
            case "bankloan":
                return "id AS loanbillid,org AS p8z9_org,bizdate,currency AS p8z9_srccur,drawamount,p8z9_tzll,finproduct.id";
            case "":// 作为后续融资租赁的冗余
                return "";
        }
        throw new KDException("不存在的数据源");
    }

    /**
     * 构建分类表达式
     *
     * @param maxLevel
     * @return
     */
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

    /**
     * 根据层级获取对应的 fullname 字段路径
     *
     * @param level
     * @return
     */
    private String getFullNameFieldByLevel(int level) {
        StringBuilder path = new StringBuilder("fullname");
        for (int i = 1; i < level; i++) {
            path.insert(0, "parent.");
        }
        return path.toString();
    }

    /**
     * 获取融资品种最大级次
     *
     * @return
     */
    private int getVarietyMaxLevel() {
        DynamicObjectCollection dc = QueryServiceHelper.query(this.getClass().getName(),
                "cfm_financingvarieties",
                "level",
                null,
                "level desc",
                1);
        if (dc.size() > 0) {
            return dc.get(0).getInt("level");
        } else {
            logger.error("查询融资品种最大级次失败");
            throw new KDBizException("查询融资品种最大级次失败");
        }
    }

    /**
     * 获取担保占用
     * @param loanBillIds
     * @return
     */
    private DataSet queryGContractDs(List<Long> loanBillIds) {
        QFilter gContractQFilter = new QFilter("gsrcbillid", "in", loanBillIds);
        String gContractSelProps = "id,gsrcbillno,gsrcbillid,gcontract.guaranteeway AS guaranteeway,gratio";
        return QueryServiceHelper.queryDataSet(getClass().getName(), "gm_guaranteeuse", gContractSelProps, gContractQFilter.toArray(), null);
    }
}