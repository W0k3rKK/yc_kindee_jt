package p8z9.jtuat.tmc.cfm.plugin.report;

import cn.hutool.core.date.DateUtil;
import kd.bos.algo.DataSet;
import kd.bos.entity.report.AbstractReportListDataPlugin;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.sdk.plugin.Plugin;
import kd.tmc.cfm.common.enums.LoanTypeEnum;
import kd.tmc.cfm.report.helper.ReportCommonHelper;
import kd.tmc.cfm.report.helper.TradeFinanceFilterHelper;
import kd.tmc.cfm.report.helper.TradeFinanceRptHelper;
import kd.tmc.fbp.common.util.EmptyUtil;

import java.util.*;

/**
 * 本年融资情况表
 * 报表取数插件
 *
 * @see kd.tmc.cfm.report.data.TradeFinanceDataListPlugin#queryDataSet
 * @see TradeFinanceFilterHelper#interestRateDS(List, Date, Class)
 */
public class FinancingOfTheYearDataListPlugin extends AbstractReportListDataPlugin implements Plugin {

    private Date cutoffdate;

    @Override
    public DataSet query(ReportQueryParam queryParam, Object o) throws Throwable {

        Map<String, Object> paramMap = ReportCommonHelper.transQueryParam(queryParam);
        // 初始化成员变量
        this.initParam(paramMap);

        // 根据数据源查询单据（含通用过滤条件
        DataSet dataSet = this.queryLoanBillDS(queryParam, paramMap);

        // 1、小计行构建
        String[] sumFields = new String[]{"p8z9_dutyunit", "p8z9_finproduct", "p8z9_guarantee", "p8z9_bizdate", "p8z9_term", "p8z9_billno",
                "p8z9_srccur", "p8z9_basecur", "p8z9_interest", "p8z9_baseinterest", "p8z9_drawamount", "p8z9_intrate", "p8z9_totalcostratio", "p8z9_isrenewal", "p8z9_description",
                "p8z9_loanbillid"};
        DataSet dataSetSum = dataSet.copy().groupBy(new String[]{"p8z9_org"}).sum("p8z9_drawamount").sum("p8z9_amount").sum("p8z9_baseamount").sum("p8z9_principal").finish();
        dataSetSum = dataSetSum.addField("'小计'", "p8z9_textcreditor");//将“小计”字段添加到String字段类型中
        dataSetSum = dataSetSum.addField("'1'", "p8z9_sumlevel");
        dataSetSum = dataSetSum.addNullField(sumFields);
        dataSetSum = dataSetSum.select(this.getReportFields());//对小计DS的字段进行排序以对应原DS，进行下一步的union
        dataSet = dataSet.union(dataSetSum).orderBy(new String[]{"p8z9_org", "p8z9_sumlevel"});// union默认不会剔除重复数据

        // 2、合计行构建
        String[] totalSumFields = new String[]{"p8z9_org", "p8z9_dutyunit", "p8z9_finproduct", "p8z9_guarantee", "p8z9_bizdate", "p8z9_term", "p8z9_billno",
                "p8z9_srccur", "p8z9_basecur", "p8z9_interest", "p8z9_baseinterest", "p8z9_drawamount", "p8z9_intrate", "p8z9_totalcostratio", "p8z9_isrenewal", "p8z9_description",
                "p8z9_loanbillid"};
        DataSet dataSetTotalSum = dataSet.copy().filter("p8z9_sumlevel = '1'").groupBy(null).sum("p8z9_drawamount").sum("p8z9_amount").sum("p8z9_baseamount").sum("p8z9_principal").finish();
        dataSetTotalSum = dataSetTotalSum.addField("'合计'", "p8z9_textcreditor");//将“合计”字段添加到String字段类型中
        dataSetTotalSum = dataSetTotalSum.addField("'2'", "p8z9_sumlevel");
        dataSetTotalSum = dataSetTotalSum.addNullField(totalSumFields);
        dataSetTotalSum = dataSetTotalSum.select(this.getReportFields());//对合计DS的字段进行排序以对应原DS，进行下一步的union
        dataSet = dataSet.union(dataSetTotalSum);


        return dataSet;
    }

    private void initParam(Map<String, Object> paramMap) {
        this.cutoffdate = (Date) paramMap.get("p8z9_filter_cutoffdate");
    }

    private DataSet queryLoanBillDS(ReportQueryParam queryParam, Map<String, Object> paramMap) {

        QFilter loanBillQFilter = TradeFinanceFilterHelperExt.loanBillQFilter(queryParam);

        // 本年末
        // loanBillQFilter.and(new QFilter("bizdate", "<=", DateUtil.endOfYear(this.cutoffdate)));
        // 本年初
        loanBillQFilter.and(new QFilter("bizdate", ">=", DateUtil.beginOfYear(this.cutoffdate)));
        QFilter elFilter = loanBillQFilter.copy();
        QFilter bondQFilter = loanBillQFilter.copy();
        QFilter blQFilter = loanBillQFilter.copy();

        List<DataSet> dataSetList = new ArrayList<>();
        String dataSource = paramMap.get("p8z9_filter_datasource").toString();

        // 债券发行
        QFilter slCredFilter;
        if (dataSource.contains(LoanTypeEnum.BOND.getValue())) {
            slCredFilter = TradeFinanceRptHelper.getLoanTypeFilter(LoanTypeEnum.BOND.getValue());
            bondQFilter.and(slCredFilter);
            String zwfxSelectFields = this.getSelectFields(LoanTypeEnum.BOND.getValue());
            DataSet bondDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill_bond", zwfxSelectFields, bondQFilter.toArray(), null);
            dataSetList.add(bondDS);
        }

        // 融资租赁
        DataSet loanBillDS;
        if (dataSource.contains("fl")) {
            //     如使用融资租赁，需要检查在单据上是否有字段融资项目责任部门p8z9_dutyunit
        }

        // 企业提款
        if (dataSource.contains("entrustloan")) {
            elFilter.and(new QFilter("loantype", "in", Arrays.asList("entrust", "ec")));
            String qytkSelectFields = this.getSelectFields("entrustloan");
            loanBillDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill", qytkSelectFields, elFilter.toArray(), null);
            dataSetList.add(loanBillDS);
        }


        // 银行提款
        if (dataSource.contains("bankloan")) {
            blQFilter.and("loantype", "=", LoanTypeEnum.BANKLOAN.getValue());
            String yhtkSelectFields = this.getSelectFields("bankloan");
            loanBillDS = QueryServiceHelper.queryDataSet(this.getClass().getName(), "cfm_loanbill", yhtkSelectFields, blQFilter.toArray(), null);
            dataSetList.add(loanBillDS);
        }
        DataSet dataSet = EmptyUtil.isEmpty(dataSetList) ? null : dataSetList.stream().reduce(DataSet::union).get();
        if (Objects.isNull(dataSet)) {
            return null;
        }
        dataSet = dataSet.orderBy(new String[]{"p8z9_org"});

        /*// 获取单据id集合
        List<Long> loanBillIds = TradeFinanceFilterHelper.getloanBillIds(dataSet, "p8z9_loanbillid");

        // 获取浮动利率
        DataSet interestRateDS = TradeFinanceFilterHelperExt.interestRateDS(loanBillIds, this.cutoffdate, this.getClass());
        dataSet = dataSet.leftJoin(interestRateDS).on("p8z9_loanbillid", "p8z9_loanbillid").select(dataSet.getRowMeta().getFieldNames(), new String[]{"p8z9_intrate"}).finish();*/

        // 计算金额本位币、金额公式
        String curUnit = paramMap.get("p8z9_filter_currencyunit").toString();// 货币单位
        String curField = "p8z9_srccur";
        DataSet rateDs = TradeFinanceFilterHelperExt.getExChangeRateDs(dataSet, curField, 1L, this.cutoffdate);
        dataSet = dataSet.addNullField("p8z9_principal", "p8z9_interest", "p8z9_baseinterest", "p8z9_baseamount");// 需计算的金额字段，本金、利息（原币）、利息（本位币）、融资总额（本位币）
        dataSet = dataSet.addField("'0'", "p8z9_sumlevel");
        // 连接需查询的字段，查出汇率进行计算
        List<String> joinSelPropList = new ArrayList<>();
        Collections.addAll(joinSelPropList, this.getReportFields());
        joinSelPropList.add("tarcurrency");
        joinSelPropList.add("rate");
        dataSet = dataSet.leftJoin(rateDs).on(curField, "tarcurrency").select(joinSelPropList.toArray(new String[0])).finish();
        dataSet = dataSet.updateField("p8z9_drawamount", "p8z9_drawamount /" + curUnit);// 实际融资额（原币）
        dataSet = dataSet.updateField("p8z9_principal", "p8z9_drawamount * rate");// 本金 为实际融资额的本位币
        dataSet = dataSet.updateField("p8z9_interest", "p8z9_drawamount * p8z9_totalcostratio");// 利息（原币）=实际融资额（原币）*总成本率
        dataSet = dataSet.updateField("p8z9_baseinterest", "p8z9_principal * p8z9_totalcostratio * rate");// 利息（本位币）=本金*总成本率
        dataSet = dataSet.updateField("p8z9_amount", "p8z9_amount /" + curUnit);// 融资总额（原币）
        dataSet = dataSet.updateField("p8z9_baseamount", "p8z9_amount * rate");// 融资总额（本位币）
        dataSet = dataSet.select(this.getReportFields());

        return dataSet;
    }

    /**
     * 根据数据源组合成直接从数据库查询的字段
     *
     * @param dataSource 数据源
     * @return
     */
    private String getSelectFields(String dataSource) {
        StringBuilder commonSelFields = new StringBuilder();
        commonSelFields.append("org AS p8z9_org, ") //融资主体
                .append("CASE WHEN p8z9_dutyunit IS NULL OR p8z9_dutyunit = 0 THEN org ELSE p8z9_dutyunit END AS p8z9_dutyunit, ")  //融资项目责任部门
                .append("finproduct.name AS p8z9_finproduct, ") //交易模式
                .append("bizdate AS p8z9_bizdate, ")    //到账时间
                .append("term AS p8z9_term, ")  //期限
                .append("billno AS p8z9_billno, ")
                .append("currency AS p8z9_srccur, ")
                .append("1 AS p8z9_basecur, ")
                .append("drawamount AS p8z9_drawamount, ")
                .append("p8z9_tzll AS p8z9_intrate, ")
                .append("amount AS p8z9_amount, ")
                .append("p8z9_tzll AS p8z9_totalcostratio, ")
                .append("description AS p8z9_description, ")
                .append("id as p8z9_loanbillid, ")
                .append("loancontractbill.p8z9_radiooptgroupfield1 AS p8z9_isrenewal, ");//是否续贷项目，提款处理和债券发行取值路径相同
        switch (dataSource) {
            // 债权人、担保方式
            case "bond":
                return commonSelFields.append("investor_entry.e_investorname AS p8z9_textcreditor, ")
                        .append("guaranteeway AS p8z9_guarantee")
                        .toString();
            // 债权人、担保方式
            case "entrustloan":
            case "bankloan":
                return commonSelFields.append("textcreditor AS p8z9_textcreditor, ")
                        .append("loancontractbill.guarantee AS p8z9_guarantee")
                        .toString();

        }
        return commonSelFields.toString();
    }

    /**
     * @return 需要报表取数字段，按顺序排列
     */
    private String[] getReportFields() {
        // 融资主体	融资机构	融资项目责任部门	交易模式	增信措施	到账时间	期限	实际融资额/万元	融资总额/万元	利息（原币）  利息（本位币）	本金 合同利率	总成本率 是否续贷项目	备注  合计排序    提款单id
        return new String[]{"p8z9_org", "p8z9_textcreditor", "p8z9_dutyunit", "p8z9_finproduct", "p8z9_guarantee", "p8z9_bizdate",
                "p8z9_term", "p8z9_billno", "p8z9_srccur", "p8z9_basecur", "p8z9_drawamount", "p8z9_amount", "p8z9_baseamount", "p8z9_interest", "p8z9_baseinterest",
                "p8z9_principal", "p8z9_intrate", "p8z9_totalcostratio", "p8z9_isrenewal", "p8z9_description", "p8z9_sumlevel", "p8z9_loanbillid"};
    }

}