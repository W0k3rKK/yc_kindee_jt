package p8z9.jtuat.tmc.cfm.plugin.form;

import com.twelvemonkeys.util.LinkedSet;
import kd.bos.base.AbstractBasedataController;
import kd.bos.form.field.events.BaseDataCustomControllerEvent;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;

/**
 * 用于测试基础资料的全局过滤
 * 废弃
 */
@Deprecated
public class BaseDataFilterPlugin extends AbstractBasedataController {

    @Override
    public void buildBaseDataCoreFilter(BaseDataCustomControllerEvent evt) {
        super.buildBaseDataCoreFilter(evt);

        String[] status = {"A", "B", "C"};
        evt.addQFilter(new QFilter("status", QCP.in, status));
    }
}
