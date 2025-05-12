package p8z9.jtuat.tmc.bdim.plugin.form;

import kd.bos.form.plugin.AbstractFormPlugin;
import kd.sdk.plugin.Plugin;

import java.util.EventObject;

/**
 * 债券注册额度 审核中（提交后）可修改部分字段
 */
public class EnableFieldsAfterSubmitPlugin extends AbstractFormPlugin implements Plugin {

    @Override
    public void afterBindData(EventObject e) {
        super.afterBindData(e);
        this.getView().setEnable(true, new String[]{"fs_baseinfo"});
    }
}