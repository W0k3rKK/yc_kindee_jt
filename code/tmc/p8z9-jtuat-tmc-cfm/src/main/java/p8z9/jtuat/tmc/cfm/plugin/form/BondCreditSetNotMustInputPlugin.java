package p8z9.jtuat.tmc.cfm.plugin.form;

import kd.bos.entity.datamodel.events.PropertyChangedArgs;
import kd.bos.form.IFormView;
import kd.bos.form.field.FieldEdit;
import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 债券发行申请中发行日期，结束日期，期限改为非必填
 */
public class BondCreditSetNotMustInputPlugin extends AbstractFormPlugin implements Plugin {

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.registerMustInput(false,"startdate","term","enddate");
    }

    @Override
    public void propertyChanged(PropertyChangedArgs e) {
        super.propertyChanged(e);
        String changeName = e.getProperty().getName();
        if ("finproduct".equals(changeName)) {
            this.registerMustInput(false,"startdate","term","enddate");
        }

    }

    /**
     * 字段是否必录
     * @param isMustInput 是否必录
     * @param propNameArr 字段标识
     */
    private void registerMustInput(boolean isMustInput, String... propNameArr) {
        IFormView view = this.getView();
        String[] arr = propNameArr;
        int arrLength = propNameArr.length;

        for (int length = 0; length < arrLength; ++length) {
            String propName = arr[length];
            FieldEdit fieldEdit = (FieldEdit) view.getControl(propName);
            fieldEdit.setMustInput(isMustInput);
        }
    }


}