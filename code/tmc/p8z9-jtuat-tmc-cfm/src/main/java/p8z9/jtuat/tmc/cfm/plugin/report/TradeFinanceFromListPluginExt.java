package p8z9.jtuat.tmc.cfm.plugin.report;

import com.tongtech.jms.ra.util.Exc;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.events.PackageDataEvent;
import kd.bos.entity.report.ReportQueryParam;
import kd.bos.report.events.SummaryEvent;
import kd.bos.report.plugin.AbstractReportFormPlugin;
import kd.sdk.plugin.Plugin;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表界面插件
 * 用于处理金融交易相关报表数据的行数据汇总计算，包括“小计”、“合计”等特殊行的处理。
 */
public class TradeFinanceFromListPluginExt extends AbstractReportFormPlugin implements Plugin {
    /**
     * 存储各字段的累加值，用于“小计”行。
     */
    private final Map<String, BigDecimal> fieldSums = new HashMap<>();

    /**
     * 存储各字段的总累加值，用于“合计”行。
     */
    private final Map<String, BigDecimal> totalSums = new HashMap<>();

    /**
     * 初始化字段累加器，将所有需要统计的字段初始化为 0。
     */
    private void initSums() {
        // 小计字段初始化
        fieldSums.put("contractamt", BigDecimal.ZERO);
        fieldSums.put("drawamount", BigDecimal.ZERO);
        fieldSums.put("paidamt", BigDecimal.ZERO);
        fieldSums.put("unpaidamt", BigDecimal.ZERO);
        fieldSums.put("estinterestamt", BigDecimal.ZERO);
        fieldSums.put("paidintamt", BigDecimal.ZERO);
        fieldSums.put("unpaidintamt", BigDecimal.ZERO);

        // 合计字段初始化（仅第一次初始化）
        if (totalSums.isEmpty()) {
            totalSums.put("contractamt", BigDecimal.ZERO);
            totalSums.put("drawamount", BigDecimal.ZERO);
            totalSums.put("paidamt", BigDecimal.ZERO);
            totalSums.put("unpaidamt", BigDecimal.ZERO);
            totalSums.put("estinterestamt", BigDecimal.ZERO);
            totalSums.put("paidintamt", BigDecimal.ZERO);
            totalSums.put("unpaidintamt", BigDecimal.ZERO);
        }
    }

    @Override
    public void processRowData(String gridPK, DynamicObjectCollection rowData, ReportQueryParam queryParam) {
        super.processRowData(gridPK, rowData, queryParam);
        if (rowData.isEmpty()) return;

        // 初始化字段累加器
        initSums();

        // 第一遍遍历：处理普通行和小计行
        for (DynamicObject obj : rowData) {
            String term = obj.getString("term");

            if ("小计".equals(term)) {
                // 将当前累计值写入“小计”行，并重置
                setAmt(obj);
                initSums();
            } else if ("合计".equals(term)) {
                // 合计行暂不处理
                continue;
            } else {
                // 普通行：进行金额累加
                accumulate(obj);
            }
        }

        // 第二遍遍历：处理“合计”行
        for (DynamicObject obj : rowData) {
            String term = obj.getString("term");
            if ("合计".equals(term)) {
                totalSums.forEach(obj::set);//  设置合计值
            }
        }
    }



    /**
     * 对指定行对象中的各个金额字段进行累加操作。
     *
     * @param obj 当前行数据对象
     */
    private void accumulate(DynamicObject obj) {
        // 遍历字段累加器中的每一个字段及其当前累计值
        for (Map.Entry<String, BigDecimal> entry : fieldSums.entrySet()) {
            // 获取字段名
            String field = entry.getKey();

            // 获取该字段当前已累计的值（可能是之前几行的总和）
            BigDecimal value = entry.getValue();

            // 从当前行中获取该字段的实际值
            BigDecimal val = obj.getBigDecimal(field);

            // 如果该字段在当前行中有值（非 null），则进行累加
            if (val != null) {
                // 更新该字段的累加值（用于小计）：原值 + 当前行的值
                fieldSums.put(field, value.add(val));
                // 同步更新 totalSums（用于合计）
                totalSums.put(field, totalSums.get(field).add(val));
            }
        }
    }


    /**
     * 将当前累计的字段值设置到目标行对象上。
     *
     * @param rowDataOne 目标行对象（如“合计”或“小计”行）
     */
    private void setAmt(DynamicObject rowDataOne) {
        fieldSums.forEach(rowDataOne::set);
    }

}