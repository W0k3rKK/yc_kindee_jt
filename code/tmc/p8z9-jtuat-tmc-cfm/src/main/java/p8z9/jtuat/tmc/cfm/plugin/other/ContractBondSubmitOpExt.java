package p8z9.jtuat.tmc.cfm.plugin.other;

import kd.tmc.cfm.business.validate.bill.BusinessInfoSaveValidator;
import kd.tmc.cfm.opplugin.contractbill.ContractBondSubmitOp;
import kd.tmc.fbp.business.validate.AbstractTmcBizOppValidator;

/**
 * 债券发行申请
 * 移除标品插件中 getBizOppValidators方法的 ContractBondSubmitValidator
 * 去除期限(ymd)、结束日期必录限制
 */
public class ContractBondSubmitOpExt extends ContractBondSubmitOp {

    public AbstractTmcBizOppValidator[] getBizOppValidators() {
        return new AbstractTmcBizOppValidator[]{new BusinessInfoSaveValidator()};
    }
}