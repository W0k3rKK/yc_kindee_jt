package p8z9.jtuat.tmc.fbd.plugin.form;

import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.dataentity.utils.StringUtils;
import kd.bos.entity.datamodel.IDataModel;
import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.IFormView;
import kd.bos.form.control.EntryGrid;
import kd.bos.form.control.OperationColumn;
import kd.bos.form.operatecol.OperationColItem;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

/**
 * 单据审核后仍启用附件分类的按钮可用性
 */
public class SetAttEnableFormPlugin extends AbstractFormPlugin implements Plugin {

    @Override
    public void initialize() {
        super.initialize();
        // IFormView view = this.getView();
        EntryGrid entryGrid = (EntryGrid) this.getControl("entryentity");
        entryGrid.addPackageDataListener((pkEvent) -> {
                    DynamicObject rowData = pkEvent.getRowData();
                    int rowIndex = pkEvent.getRowIndex();
                    System.out.println(rowIndex);
                    Object source = pkEvent.getSource();
                    if (source instanceof OperationColumn) {
                        String key = ((OperationColumn) source).getKey();
                        List<OperationColItem> operationColItems = (List) pkEvent.getFormatValue();
                        switch (key) {
                            case "uploadoperationcolumn":
                                for (OperationColItem operationColItem : operationColItems) {
                                    operationColItem.setLocked(false);
                                }
                                break;
                            case "operationcolumnap":
                                for (OperationColItem operationColItem : operationColItems) {
                                    DynamicObjectCollection attaches = rowData.getDynamicObjectCollection("attachment");
                                    if (attaches != null && !attaches.isEmpty() && !"mark".equals(operationColItem.getOperationKey())) {
                                        operationColItem.setLocked(false);
                                        operationColItem.setVisible(true);
                                    }
                                }
                                break;
                        }

                    }
                }
        );
    }
}