package p8z9.jtuat.tmc.cfm.plugin.form;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.util.StringUtils;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.Objects;

/**
 * 批量结息处理 结息记录分录上新增“合同号”、“债券名称”字段，以及需要新增合计数
 * 动态表单插件
 */
public class InterestBillBatchEdit extends AbstractFormPlugin implements Plugin {

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        IDataModel model = this.getModel();

        String entityNumber = "";
        String datasource = (String) model.getValue("datasource");//数据来源
        switch (datasource) {
            case "cfm":
                entityNumber = "cfm_loanbill";
                break;
            case "bond":
                entityNumber = "cfm_loanbill_bond";
                break;
        }

        if (StringUtils.isEmpty(entityNumber)) {
            return;
        }

        DynamicObjectCollection entry = (DynamicObjectCollection) model.getValue("entry");//结息记录

        for (int i = 0; i < entry.size(); i++) {
            Object contractBillNo = model.getValue("loanbillno", i);
            if (Objects.isNull(contractBillNo)) {
                continue;
            }

            // 获取合同名称或债券名称，字段标识相同，实体编码不同
            DynamicObject contractBill = QueryServiceHelper.queryOne(entityNumber, "contractname",
                    new QFilter("billno", "=", contractBillNo).toArray());
            if (Objects.isNull(contractBill)) {
                continue;
            }
            String contractName = contractBill.getString("contractname");
            model.setValue("p8z9_contractname", contractName, i);//合同名称
        }
    }
}