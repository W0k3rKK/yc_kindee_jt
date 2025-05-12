package p8z9.jtuat.tmc.cfm.plugin.form;

import kd.bos.bill.BillShowParameter;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.datamodel.ListSelectedRow;
import kd.bos.entity.datamodel.ListSelectedRowCollection;
import kd.bos.form.ShowType;
import kd.bos.form.control.events.ItemClickEvent;
import kd.bos.list.IListView;
import kd.bos.list.plugin.AbstractListPlugin;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.Objects;

/**
 * 债券注册额度列表
 * 新增发行申请按钮，可以直接通过列表进行发行申请；将债券注册额度单中的批文编码带到债券发行申请中
 *
 * @see LoanBillSetDefaultValueBillPlugin
 */
public class OpenLoanBillListPlugin extends AbstractListPlugin implements Plugin {

    private static Log log = LogFactory.getLog(OpenLoanBillListPlugin.class);


    // 当前列表按钮标识
    private static final String ITEM_BTN_KEY = "p8z9_openloanbill";
    // 当前列表单据字段
    public static final String FIELD_KEY = "p8z9_textfield";
    // 待打开表单标识
    private static final String BILL_KEY = "cfm_loancontract_bo";

    @Override
    public void registerListener(EventObject e) {
        super.registerListener(e);
        this.addItemClickListeners(ITEM_BTN_KEY);
    }

    @Override
    public void itemClick(ItemClickEvent evt) {
        super.itemClick(evt);

        String itemKey = evt.getItemKey();
        if (!ITEM_BTN_KEY.equals(itemKey)) {
            return;
        }

        ListSelectedRowCollection rows = this.getSelectedRows();
        if (rows.size() != 1) {
            this.getView().showTipNotification("仅能选择一条数据且必须选择一条数据！");
            return;
        }

        // 构建单据页面
        BillShowParameter billShowParameter = new BillShowParameter();
        billShowParameter.setFormId(BILL_KEY);
        billShowParameter.getOpenStyle().setShowType(ShowType.MainNewTabPage);

        // 限定选择一行所以直接获取首行
        ListSelectedRow rowOne = rows.get(0);
        Object pkId = rowOne.getPrimaryKeyValue();
        String billFormId = ((IListView) this.getView()).getBillFormId();

        // 根据单据id查询字段值
        DynamicObject queryOne = QueryServiceHelper.queryOne(billFormId, FIELD_KEY, new QFilter("id", QCP.equals, pkId).toArray());
        String fieldValue = "";
        if (Objects.isNull(queryOne) || queryOne.getString(FIELD_KEY) == null) {
            this.getView().showTipNotification("选中行中未查询到字段值");
            log.info("选中行{}中未查询到字段值", rowOne.getBillNo());
        } else {
            fieldValue = queryOne.getString(FIELD_KEY);
        }

        billShowParameter.setCustomParam(FIELD_KEY, fieldValue);

        this.getView().showForm(billShowParameter);

    }
}