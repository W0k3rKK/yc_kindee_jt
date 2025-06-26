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
import kd.tmc.fbp.common.enums.BillStatusEnum;
import kd.tmc.fbp.common.util.EmptyUtil;

import java.util.*;

/**
 * 融资分类统计表（本年融资情况汇总表）
 * 报表取数插件
 */
public class FinancingClassificationStatisticsDataListPlugin extends AbstractReportListDataPlugin implements Plugin {

    private static final Log logger = LogFactory.getLog(FinancingClassificationStatisticsDataListPlugin.class);

    // 查询年份
    private Date cutoffdate;
    // 统计维度
    private String value_dimension;
    // 报表过滤条件
    private Map<String, Object> paramMap;

    private void initParam(ReportQueryParam queryParam) {
        this.paramMap = ReportCommonHelper.transQueryParam(queryParam);
        this.cutoffdate = (Date) this.paramMap.get("p8z9_filter_cutoffdate");
        this.value_dimension = (String) this.paramMap.get("p8z9_filter_dimension");
    }

    @Override
    public DataSet query(ReportQueryParam queryParam, Object selectedObj) throws Throwable {
        // 初始化成员变量
        this.initParam(queryParam);

        // 根据不同统计维度，进行不同数据查询及组装
        DataSet dataSet = null;
        switch (value_dimension) {
            case "financing_model":
            case "financing_subject":
                String groupByField = null;
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
                DataSet DS = this.getModelDs(queryParam);
                // 按维度小计
                dataSet = DS.copy().groupBy(new String[]{groupByField})
                        .sum("p8z9_baseamt")// 融资余额（本位币）汇总
                        .sum("p8z9_baseprincipal")// 本金（本位币）汇总
                        .sum("p8z9_baseinterest")// 利息（本位币）汇总
                        .finish();
                dataSet = dataSet.addField("1", "p8z9_basecur");// 本位币赋值
                break;
            case "credit_method":
                // 增信方式
                // 根据担保方式进行分组并汇总金额
                DataSet methodDs = this.getMethodDs();
                return methodDs;
        }

        return dataSet;
    }

    private DataSet getMethodDs() {

        // 查询数据
        DataSet methodDS = this.queryMethodDS();

        // 处理金额等数据
        methodDS = this.calMethod(methodDS);
        methodDS = methodDS.addField("'1'", "p8z9_basecur");// 本位币

        return methodDS;
    }

    private DataSet queryMethodDS() {
        QFilter billStatusQ = new QFilter("gcontract.billstatus", "=", BillStatusEnum.AUDIT.getValue());// 担保合同.单据状态 = 已审核
        QFilter bizStatusQ = new QFilter("gcontract.bizstatus", "=", "doing");// 担保合同.业务状态 = 执行中
        QFilter dateQ = new QFilter("gcontract.bizdate", ">=", DateUtil.beginOfYear(this.cutoffdate));// 起始日期大于等于本年
        String selFields = "gdebtcurrency, gdebtbalance, gcontract.guaranteevarieties.name AS p8z9_guarantee";
        List<DataSet> dataSetList = new ArrayList<>();

        // 将包含保证的担保品种都归为 保证
        DataSet ensureDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "gm_guaranteeuse", selFields,
                new QFilter[]{bizStatusQ, billStatusQ, dateQ, new QFilter("gcontract.guaranteevarieties.name", "like", "保证%")}, null);
        ensureDS = ensureDS.updateField("p8z9_guarantee", "'保证'");
        dataSetList.add(ensureDS);

        // 其余担保品种
        DataSet otherDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "gm_guaranteeuse", selFields,
                new QFilter[]{bizStatusQ, billStatusQ, dateQ, new QFilter("gcontract.guaranteevarieties.name", "not like", "保证%")}, null);
        dataSetList.add(otherDS);

        DataSet methodDS = EmptyUtil.isEmpty(dataSetList) ? null : dataSetList.stream().reduce(DataSet::union).get();

        return methodDS;
    }

    private DataSet calMethod(DataSet methodDS) {
        // 计算金额本位币、金额公式
        String curUnit = paramMap.get("p8z9_filter_currencyunit").toString();// 货币单位
        String curField = "gdebtcurrency";
        DataSet rateDs = TradeFinanceFilterHelperExt.getExChangeRateDs(methodDS, curField, 1L, this.cutoffdate);// 汇率
        List<String> joinSelPropList = new ArrayList<>();
        Collections.addAll(joinSelPropList, methodDS.getRowMeta().getFieldNames());
        joinSelPropList.add("tarcurrency");
        joinSelPropList.add("rate");
        methodDS = methodDS.leftJoin(rateDs).on(curField, "tarcurrency").select(methodDS.getRowMeta().getFieldNames(), new String[]{"rate"}).finish();
        methodDS = methodDS.addField("gdebtbalance * rate / " + curUnit, "p8z9_baseamt");
        methodDS = methodDS.groupBy(new String[]{"p8z9_guarantee"}).sum("p8z9_baseamt").finish();

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
        modelDS = varietyDs.join(modelDS).on("finproduct.id", "finproduct.id").select(modelDS.getRowMeta().getFieldNames(), new String[]{"category AS p8z9_rzms"}).finish();

        return modelDS;
    }

    private DataSet calModelAmt(DataSet modelDS) {
        // 单据id集合 全部
        // List<Long> allModelLoanBillIds = TradeFinanceFilterHelper.getloanBillIds(modelDS, "loanbillid");
        // 融资余额（原币） 取时点的未还本金汇总，全部
        // DataSet allModelPayAmtDS = TradeFinanceFilterHelperExt.enotrePayAmtDS(allModelLoanBillIds, cutoffdate, "loanbillid", "p8z9_amt", this.getClass());
        // 将融资余额（原币）join进主DataSet
        // modelDS = modelDS.join(allModelPayAmtDS).on("loanbillid", "loanbillid").select(modelDS.getRowMeta().getFieldNames(), new String[]{"p8z9_amt"}).finish();

        // 单据id集合 年度
        // DataSet yearModelDS = modelDS.copy().filter("bizdate >= '" + DateUtil.beginOfYear(this.cutoffdate) + "'");
        List<Long> yearModelLoanBillIds = TradeFinanceFilterHelper.getloanBillIds(modelDS, "loanbillid");
        // 本金（原币） 取时点的未还本金汇总，年度
        DataSet yearPayAmtDS = TradeFinanceFilterHelperExt.enotrePayAmtDS(yearModelLoanBillIds, this.cutoffdate, "loanbillid", "p8z9_amt", this.getClass());
        // 将本金（原币）join进主DataSet
        modelDS = modelDS.join(yearPayAmtDS).on("loanbillid", "loanbillid").select(modelDS.getRowMeta().getFieldNames(), new String[]{"p8z9_amt"}).finish();

        // 计算金额本位币、金额公式
        String curUnit = paramMap.get("p8z9_filter_currencyunit").toString();// 货币单位
        String curField = "p8z9_srccur";
        DataSet rateDs = TradeFinanceFilterHelperExt.getExChangeRateDs(modelDS, curField, 1L, this.cutoffdate);
        modelDS = modelDS.join(rateDs).on("p8z9_srccur", "tarcurrency").select(modelDS.getRowMeta().getFieldNames(), new String[]{"rate"}).finish();
        modelDS = modelDS.addField("p8z9_amt * rate / " + curUnit, "p8z9_baseamt");//融资余额（本位币）
        modelDS = modelDS.addField("p8z9_baseamt", "p8z9_baseprincipal");//本金（本位币） 目前等于融资余额（本位币）
        modelDS = modelDS.addField("drawamount * p8z9_tzll * rate / " + curUnit, "p8z9_baseinterest");//利息（本位币）
        modelDS = modelDS.addField("CASE WHEN p8z9_baseprincipal <> 0 AND p8z9_baseprincipal IS NOT NULL THEN p8z9_baseinterest / p8z9_baseprincipal ELSE 0 END", "p8z9_avg_interestrate");//平均利率成本 = 利息（本位币） / 本金（本位币）

        return modelDS;
    }

    private DataSet queryModelDS(ReportQueryParam queryParam) {
        QFilter loanBillQFilter = TradeFinanceFilterHelperExt.loanBillQFilter(queryParam);
        // 本年末
        // loanBillQFilter.and(new QFilter("bizdate", "<=", DateUtil.endOfYear(this.cutoffdate)));
        // 本年初
        loanBillQFilter.and(new QFilter("bizdate", ">=", DateUtil.beginOfYear(this.cutoffdate)));
        QFilter elFilter = loanBillQFilter.copy();
        QFilter bondQFilter = loanBillQFilter.copy();
        QFilter blQFilter = loanBillQFilter.copy();

        List<DataSet> dataDSList = new ArrayList<>();
        String dataSource = paramMap.get("p8z9_filter_datasource").toString();

        // 债券发行
        QFilter slCredFilter;
        if (dataSource.contains(LoanTypeEnum.BOND.getValue())) {
            slCredFilter = TradeFinanceRptHelper.getLoanTypeFilter(LoanTypeEnum.BOND.getValue());
            bondQFilter.and(slCredFilter);
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
            blQFilter.and("loantype", "=", LoanTypeEnum.BANKLOAN.getValue());
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
            case ""://作为后续融资租赁的冗余
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
}