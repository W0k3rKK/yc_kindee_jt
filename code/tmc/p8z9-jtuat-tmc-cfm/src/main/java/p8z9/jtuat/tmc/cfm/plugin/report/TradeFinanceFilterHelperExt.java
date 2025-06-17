package p8z9.jtuat.tmc.cfm.plugin.report;

import kd.bos.algo.DataSet;
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

public class TradeFinanceFilterHelperExt {

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

        // qFilter.and(initBizdateFitler(paramMap));
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
        String bizDate = (String) paramMap.get(ISVServiceHelper.getISVInfo().getId() + "_bizdateranges");
        Date cutoffdate = ReportCommonHelper.getCutOffDate(paramMap);
        cutoffdate = DateUtils.truncateDate(cutoffdate == null ? DateUtils.getCurrentDate() : cutoffdate);
        QFilter filter = new QFilter("bizdate", "<", DateUtils.getNextDay(cutoffdate, 1));
        Date currentDate = DateUtils.getCurrentDate();
        if (!EmptyUtil.isEmpty(bizDate)) {
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
        }

        // if (RptDateRangeEnum.CUSTOM.getValue().equals(bizDate)) {
        //     Date bizDateStartDate = (Date) paramMap.get("bizdateranges_startdate");
        //     Date bizDateEndDate = (Date) paramMap.get("bizdateranges_enddate");
        //     if (!EmptyUtil.isEmpty(bizDateStartDate) && !EmptyUtil.isEmpty(bizDateEndDate)) {
        //         filter.and((new QFilter("bizdate", ">=", bizDateStartDate)).and(new QFilter("bizdate", "<=", bizDateEndDate)));
        //     }
        // }

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
}
