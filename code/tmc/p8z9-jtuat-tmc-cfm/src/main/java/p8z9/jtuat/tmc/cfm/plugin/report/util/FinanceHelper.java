package p8z9.jtuat.tmc.cfm.plugin.report.util;

import kd.bos.algo.*;
import kd.bos.context.RequestContext;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.eye.api.log.KDException;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.ISVServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.basedata.BaseDataServiceHelper;
import kd.tmc.cfm.common.enums.ConfirmStatusEnum;
import kd.tmc.cfm.common.enums.DrawTypeEnum;
import kd.tmc.cfm.report.helper.ReportCommonHelper;
import kd.tmc.fbp.common.enums.BillStatusEnum;
import kd.tmc.fbp.common.helper.TmcBusinessBaseHelper;
import kd.tmc.fbp.common.helper.TmcOrgDataHelper;
import kd.tmc.fbp.common.util.DateUtils;
import kd.tmc.fbp.common.util.EmptyUtil;
import kd.tmc.fbp.report.data.AbstractTmcTreeReportDataPlugin;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 提款单报表工具类
 * 个人专用，其他人请勿调用
 */
public class FinanceHelper {

    public static QFilter loanBillQFilter(ReportQueryParam queryParam) {
        Map<String, Object> paramMap = ReportCommonHelper.transQueryParam(queryParam);
        List<Long> queryOrgIds = getQueryOrgIds(queryParam);
        return loanBillQFilter(paramMap, queryOrgIds);
    }


    /**
     * 提款单通用筛选条件：
     * 1、单据状态 = 已审核
     * 2、确认状态 = 已确认
     * <p>
     * 额外筛选条件：
     * 1、组织（多选）
     * 2、融资品种（多选）
     * 3、币种（多选）
     *
     * @param paramMap
     * @param orgIds
     * @return
     */
    public static QFilter loanBillQFilter(Map<String, Object> paramMap, List<Long> orgIds) {
        QFilter qFilter = new QFilter("billstatus", "=", BillStatusEnum.AUDIT.getValue());
        qFilter.and(new QFilter("confirmstatus", "=", ConfirmStatusEnum.YETCONFIRM.getValue()));
        if (orgIds != null && !orgIds.isEmpty()) {
            qFilter.and(new QFilter("org.id", "in", orgIds));
        }

        QFilter noCloseOffDate = new QFilter("closeoffdate", "=", (Object) null);
        Date cutOffDate = (Date) paramMap.get("p8z9_filter_cutoffdate");
        QFilter lessThanCutOffDate = (new QFilter("closeoffdate", "<=", cutOffDate)).and(new QFilter("drawtype", "!=", DrawTypeEnum.CLOSEOUT.getValue())).and(new QFilter("notrepayamount", ">", BigDecimal.ZERO));
        QFilter largeThanCutOffDate = new QFilter("closeoffdate", ">", cutOffDate);
        qFilter.and(lessThanCutOffDate.or(largeThanCutOffDate).or(noCloseOffDate));

        qFilter.and(initBizdateFitler(paramMap));
        qFilter.and(initOrgFilter(paramMap));
        qFilter.and(initFinProductFilter(paramMap));
        qFilter.and(initCurrencyFilter(paramMap));
        return qFilter;
    }

    /**
     * 提款日期
     *
     * @param paramMap
     * @return
     */
    public static QFilter initBizdateFitler(Map<String, Object> paramMap) {
        // String bizDate = (String) paramMap.get(ISVServiceHelper.getISVInfo().getId() + "_bizdateranges");
        String bizDate = "";
        Date cutoffdate = (Date) paramMap.get("p8z9_filter_cutoffdate");
        cutoffdate = DateUtils.truncateDate(cutoffdate == null ? DateUtils.getCurrentDate() : cutoffdate);
        QFilter filter = new QFilter("bizdate", "<", DateUtils.getNextDay(cutoffdate, 1));
        /*if (!EmptyUtil.isEmpty(bizDate)) {
            switch (bizDate) {
                case "thismonth":
                    filter.and((new QFilter("bizdate", ">=", DateUtils.getFirstDayOfMonth(currentDate))).and(new QFilter("bizdate", "<", DateUtils.getFirstDateOfNextMonth(currentDate))));
                    break;
                case "thisweek":
                    filter.and((new QFilter("bizdate", ">=", DateUtils.getFirstDayOfWeek(currentDate))).and(new QFilter("bizdate", "<", DateUtils.getNextWeekMonday(currentDate))));
                    break;
                case "thisday":
                    filter.and((new QFilter("bizdate", ">=", currentDate)).and(new QFilter("bizdate", "<", DateUtils.getNextDay(currentDate, 1))));
            }
        }*/
        return filter;
    }

    public static List<Long> getQueryOrgIds(ReportQueryParam queryParam) {
        return getQueryOrgIds(queryParam, "p8z9_filter_org");
    }

    public static List<Long> getQueryOrgIds(ReportQueryParam queryParam, String orgField) {
        Long userId = RequestContext.get().getCurrUserId();
        String formId = (String) queryParam.getCustomParam().get("formid");
        String appId = (String) queryParam.getCustomParam().get("appid");
        DynamicObjectCollection col = (DynamicObjectCollection) queryParam.getFilter().getFilterItem(orgField).getValue();
        List<Long> orgIds = null;
        if (!EmptyUtil.isEmpty(col)) {
            orgIds = col.stream()
                    .map(obj -> obj.getLong("id"))
                    .collect(Collectors.toList());
        }
        // if (EmptyUtil.isEmpty((Collection) orgIds)) {
        //     orgIds = TmcOrgDataHelper.getAuthorizedBankOrgId(userId, appId, formId, "47150e89000000ac");
        // }
        /*// 是否包含下级
        if (queryParam.getFilter().containProp("filter_isincludelower") && (Boolean) queryParam.getFilter().getFilterItem("filter_isincludelower").getValue()) {
            orgIds = TmcOrgDataHelper.getAllSubordinateOrgsForCache(DEFAULTORGVIEWID, (List) orgIds, true);
        }*/

        return orgIds;
    }

    public static QFilter initOrgFilter(Map<String, Object> paramMap) {
        Set<Long> comList = new HashSet();
        Object org = paramMap.get(ISVServiceHelper.getISVInfo().getId() + "_filter_org");
        if (org instanceof DynamicObjectCollection) {
            DynamicObjectCollection companys = (DynamicObjectCollection) org;
            if (EmptyUtil.isNoEmpty(companys)) {
                Iterator var4 = companys.iterator();

                while (var4.hasNext()) {
                    DynamicObject company = (DynamicObject) var4.next();
                    comList.add(company.getLong("id"));
                }
            }
        } else if (org instanceof List) {
            comList.addAll((List) org);
        }

        if (comList.size() > 0) {
            return new QFilter("org.id", "in", comList);
        } else {
            RequestContext context = RequestContext.get();
            String appid = (String) paramMap.get("appid");
            List<Long> orgIds = TmcOrgDataHelper.getAuthorizedBankOrgIdList(context.getCurrUserId(), appid == null ? "cfm" : appid, "cfm_tradefinancerpt", "47150e89000000ac");
            return orgIds != null ? new QFilter("org.id", "in", orgIds) : null;
        }
    }

    private static QFilter initFinProductFilter(Map<String, Object> paramMap) {
        List<Object> finList = new ArrayList();
        DynamicObjectCollection finproducts = (DynamicObjectCollection) paramMap.get(ISVServiceHelper.getISVInfo().getId() + "_filter_finproduct");
        if (!EmptyUtil.isEmpty(finproducts)) {
            Iterator var3 = finproducts.iterator();

            while (var3.hasNext()) {
                DynamicObject finproduct = (DynamicObject) var3.next();
                finList.add(finproduct.getLong("id"));
            }
        }

        QFilter qfilter = null;
        if (finList.size() > 0) {
            qfilter = new QFilter("finproduct.id", "in", finList);
        }

        return qfilter;
    }

    public static QFilter initCurrencyFilter(Map<String, Object> paramMap) {
        List<Object> cnyList = new ArrayList();
        DynamicObjectCollection currencys = (DynamicObjectCollection) paramMap.get(ISVServiceHelper.getISVInfo().getId() + "_filter_currencies");
        if (!EmptyUtil.isEmpty(currencys)) {
            Iterator var3 = currencys.iterator();

            while (var3.hasNext()) {
                DynamicObject currency = (DynamicObject) var3.next();
                cnyList.add(currency.getLong("id"));
            }
        }

        QFilter qfilter = null;
        if (cnyList.size() > 0) {
            qfilter = new QFilter("currency.id", "in", cnyList);
        }

        return qfilter;
    }


    /**
     * @param srcDs
     * @param currencyField
     * @param tarCurId
     * @param date
     * @return
     * @see AbstractTmcTreeReportDataPlugin#getExChangeRateDs(DataSet, Map)
     */
    public static DataSet getExChangeRateDs(DataSet srcDs, String currencyField, Long tarCurId, Date date) {
        DataSet currencySet = srcDs.copy().groupBy(new String[]{currencyField}).finish();
        List<Long> currencyIdList = new ArrayList(10);
        currencySet.iterator().forEachRemaining((v) -> {
            currencyIdList.add(v.getLong(currencyField));
        });
        // 入参：原币id集合、目标币id、组织id、汇率日期、组织查询方式（true为组织、false为组织视图）
        return TmcBusinessBaseHelper.getExChangeDataSet(currencyIdList, tarCurId, RequestContext.get().getOrgId(), date, true);
    }


    public static BigDecimal getExchangeRate(Date ExcDate, DynamicObject srcCur) {
        // 避免因未维护人民币对人民币的汇率而报错
        if (srcCur.getLong("id") == 1L) {
            return BigDecimal.ONE;
        }
        String exchangeTableNum = "01";
        DynamicObject exchangeTable = QueryServiceHelper.queryOne("bd_exratetable", "id", new QFilter("number", "=", exchangeTableNum).toArray());
        if (Objects.isNull(exchangeTable)) {
            throw new KDException("未找到汇率表：" + exchangeTableNum);
        }
        return BaseDataServiceHelper.getExchangeRate(exchangeTable.getLong("id"),
                srcCur.getLong("id"), 1L, ExcDate);//人民币默认id为1

    }

    public static DataSet interestRateDS(List<Long> loanBillIds, Date cutoffDate, Class clazz) {
        QFilter qFilter = new QFilter("id", "in", loanBillIds);
        String field_loanBillId = "p8z9_loanbillid";
        String field_intRate = "p8z9_intrate";
        qFilter.and(new QFilter("rateadjust_entry.ra_confirmdate", "<", DateUtils.getNextDay(cutoffDate, 1)));
        String selFields = "id as " + field_loanBillId + ", rateadjust_entry.ra_yearrate as " + field_intRate + ", rateadjust_entry.ra_confirmdate as confirmdate";
        DataSet intRateDS = QueryServiceHelper.queryDataSet(clazz.getName() + "_interestrate", "cfm_loanbill", selFields, qFilter.toArray(), null);
        DataSet maxDS = intRateDS.copy().groupBy(new String[]{field_loanBillId}).max("confirmdate", "maxconfirmdate").finish();
        intRateDS = maxDS.leftJoin(intRateDS).on(field_loanBillId, field_loanBillId).on("maxconfirmdate", "confirmdate").select(field_loanBillId, field_intRate).finish();
        intRateDS = intRateDS.groupBy(new String[]{field_loanBillId}).max(field_intRate).finish();
        return intRateDS;
    }


    /**
     * 根据传入时点计算未还本金汇总，默认为未来（即大于等于时点）
     *
     * @param loanBillIds  提款单id
     * @param cutoffDate   时点日期
     * @param field_billId 报表中单据id字段标识
     * @param field_Amt    报表中未还本金字段标识
     * @param clazz        数据集类
     * @return
     */
    public static DataSet enotrePayAmtDS(List<Long> loanBillIds, Date cutoffDate, String field_billId, String field_Amt, Class clazz) {
        return enotrePayAmtDS(loanBillIds, cutoffDate, field_billId, field_Amt, clazz, ">=");
    }

    /**
     * 根据传入时点计算未还本金汇总
     *
     * @param loanBillIds  提款单id
     * @param cutoffDate   时点日期
     * @param field_billId 报表中单据id字段标识
     * @param field_Amt    报表中未还本金字段标识
     * @param clazz        数据集类
     * @param cp           比对符
     * @return
     */
    public static DataSet enotrePayAmtDS(List<Long> loanBillIds, Date cutoffDate, String field_billId, String field_Amt, Class clazz, String cp) {
        // 预计还款日期 exrepaymentdate   预计还本金 exdrawamount  未还本金 enotrepayamount
        QFilter billNoFilter = new QFilter("id", "in", loanBillIds);
        QFilter cutOffDateFilter = new QFilter("repayplan_entry.exrepaymentdate", cp, cutoffDate);
        String selProps = new StringBuilder("id AS ").append(field_billId).append(", ")
                // .append("repayplan_entry.enotrepayamount").toString();
                .append("repayplan_entry.exdrawamount").toString();
        DataSet enotrePayAmtDS = QueryServiceHelper.queryDataSet(clazz.getName(), "cfm_loanbill", selProps, new QFilter[]{billNoFilter, cutOffDateFilter}, "id");
        DataSet amtSumDS = enotrePayAmtDS.copy().groupBy(new String[]{field_billId}).sum("repayplan_entry.exdrawamount", field_Amt).finish();
        return amtSumDS;
    }

    public static DataSet createEmptyDS() {
        RowMeta rowMeta = new RowMeta(new String[]{"empty"}, new DataType[]{DataType.StringType});
        DataSet emptyDataSet = Algo.create("EmptyDataSet").createDataSetBuilder(rowMeta).build();
        return emptyDataSet;
    }


    public static List<Long> getloanBillIds(DataSet loanBillDS, String field) {
        List<Long> ids = new ArrayList(10);
        Iterator var3 = loanBillDS.copy().iterator();

        while (var3.hasNext()) {
            Row row = (Row) var3.next();
            ids.add(row.getLong(field));
        }

        return ids;
    }

    /**
     * 构造一个通用的汇总 DataSet（不包含原始数据的 union 部分）
     *
     * @param dataSet    原始数据
     * @param sumFields  需要汇总的字段
     * @param fieldOrder 最终字段顺序，可为 null 表示按dataSet字段顺序排序
     * @return 汇总后的 DataSet（字段补齐并排序）
     */
    public static DataSet buildSummaryDS(DataSet dataSet, List<String> sumFields, List<String> fieldOrder) {
        // 第一步：对数据复制并进行 groupBy + sum 汇总
        GroupbyDataSet totalSumGDS = dataSet.copy().groupBy();
        for (String sumField : sumFields) {
            totalSumGDS = totalSumGDS.sum(sumField);
        }
        DataSet sumDS = totalSumGDS.finish();

        // 第二步：获取原数据的字段名（为补齐字段做准备）
        String[] originalFieldNames = dataSet.getRowMeta().getFieldNames();
        Set<String> sumResultFields = new HashSet<>(Arrays.asList(sumDS.getRowMeta().getFieldNames()));

        // 第三步：补充 groupBy 后缺失的字段为 null 字段
        List<String> nullFields = Arrays.stream(originalFieldNames)
                .filter(f -> !sumResultFields.contains(f))
                .collect(Collectors.toList());
        if (!nullFields.isEmpty()) {
            sumDS = sumDS.addNullField(nullFields.toArray(new String[0]));
        }

        // 第四步：根据指定顺序进行字段 select（否则保持原始顺序）
        String[] selectFields;
        if (fieldOrder != null && !fieldOrder.isEmpty()) {
            selectFields = fieldOrder.toArray(new String[0]);
        } else {
            selectFields = dataSet.getRowMeta().getFieldNames();
        }
        sumDS = sumDS.select(selectFields);

        return sumDS;
    }
}