package p8z9.fi.p8z9_cas_ext.BTSyncController.tools;


import com.drew.lang.annotations.NotNull;
import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;
import java.io.Serializable;

/**
 * 付款结果
 */

@ApiModel
public class PayResultEntity implements Serializable {

    /*private static final long serialVersionUID = -5784211121408819529L;*/

    @ApiParam(value = "serial_no_erp", required = true, message = "入参【serial_no_erp】为空")
    @NotNull
    private String serial_no_erp;  //流水号
    @ApiParam(value = "sys_code", required = true, message = "入参【sys_code】为空")
    @NotNull
    private String sys_code;  //流水号
    @ApiParam(value = "erp_bill_no", required = true, message = "入参【erp_bill_no】为空")
    @NotNull
    private String erp_bill_no;  //流水号
    @ApiParam(value = "voucher_stat", required = true, message = "入参【voucher_stat】为空")
    @NotNull
    private String voucher_stat;  //凭证号
    @ApiParam(value = "returnBack_msg", required = false, message = "入参【voucher_stat】为空")
    private String returnBack_msg;
    @ApiParam(value = "payer_acc", required = true, message = "入参【payer_acc】为空")
    @NotNull
    private String payer_acc;    //付款方帐号
    @ApiParam(value = "payer_accname", required = true, message = "入参【payer_accname】为空")
    @NotNull
    private String payer_accname;  //付款方户名
    @ApiParam(value = "payer_bank", required = true, message = "入参【payer_bank】为空")
    @NotNull
    private String payer_bank; //付款方开户行
    @ApiParam(value = "payee_acc", required = true, message = "入参【payee_acc】为空")
    private String payee_acc;    //收款方账号
    @ApiParam(value = "payee_accname", required = true, message = "入参【payee_accname】为空")
    private String payee_accname;  //收款方户名
    @ApiParam(value = "payee_bank", required = true, message = "入参【payee_bank】为空")
    private String payee_bank;     //收款方开户行
    @ApiParam(value = "amt", required = true, message = "入参【amt】为空")
    @NotNull
    private String amt;       //交易金额
    @ApiParam(value = "purpose", required = true, message = "入参【purpose】为空")
    private String purpose;//用途
    @ApiParam(value = "trans_time", required = false, message = "入参【trans_time】为空")
    private String trans_time;     //交易时间   yyyy-MM-dd
    @ApiParam(value = "return_msg", required = false, message = "入参【return_msg】为空")
    private String return_msg;     //付款结果信息
    @ApiParam(value = "offline_date", required = false, message = "入参【offline_date】为空")
    private String offline_date;//离线支付日期
    @ApiParam(value = "enter_date", required = true, message = "入参【enter_date】为空")
    private String enter_date;//录入日期

    @ApiParam(value = "work_flow_id", required = true, message = "入参【work_flow_id】为空")
    private String work_flow_id;//流程名称


    /**************以下字段自定义***************************/


    public String getSerial_no_erp() {
        return serial_no_erp;
    }

    public void setSerial_no_erp(String serial_no_erp) {
        this.serial_no_erp = serial_no_erp;
    }

    public String getSys_code() {
        return sys_code;
    }

    public void setSys_code(String sys_code) {
        this.sys_code = sys_code;
    }

    public String getErp_bill_no() {
        return erp_bill_no;
    }

    public void setErp_bill_no(String erp_bill_no) {
        this.erp_bill_no = erp_bill_no;
    }

    public String getVoucher_stat() {
        return voucher_stat;
    }

    public void setVoucher_stat(String voucher_stat) {
        this.voucher_stat = voucher_stat;
    }

    public String getReturnBack_msg() {
        return returnBack_msg;
    }

    public void setReturnBack_msg(String returnBack_msg) {
        this.returnBack_msg = returnBack_msg;
    }

    public String getPayer_acc() {
        return payer_acc;
    }

    public void setPayer_acc(String payer_acc) {
        this.payer_acc = payer_acc;
    }

    public String getPayer_accname() {
        return payer_accname;
    }

    public void setPayer_accname(String payer_accname) {
        this.payer_accname = payer_accname;
    }

    public String getPayer_bank() {
        return payer_bank;
    }

    public void setPayer_bank(String payer_bank) {
        this.payer_bank = payer_bank;
    }

    public String getPayee_acc() {
        return payee_acc;
    }

    public void setPayee_acc(String payee_acc) {
        this.payee_acc = payee_acc;
    }

    public String getPayee_accname() {
        return payee_accname;
    }

    public void setPayee_accname(String payee_accname) {
        this.payee_accname = payee_accname;
    }

    public String getPayee_bank() {
        return payee_bank;
    }

    public void setPayee_bank(String payee_bank) {
        this.payee_bank = payee_bank;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getTrans_time() {
        return trans_time;
    }

    public void setTrans_time(String trans_time) {
        this.trans_time = trans_time;
    }

    public String getReturn_msg() {
        return return_msg;
    }

    public void setReturn_msg(String return_msg) {
        this.return_msg = return_msg;
    }

    public String getOffline_date() {
        return offline_date;
    }

    public void setOffline_date(String offline_date) {
        this.offline_date = offline_date;
    }

    public String getEnter_date() {
        return enter_date;
    }

    public void setEnter_date(String enter_date) {
        this.enter_date = enter_date;
    }

    public String getWork_flow_id() {
        return work_flow_id;
    }

    public void setWork_flow_id(String work_flow_id) {
        this.work_flow_id = work_flow_id;
    }

    @Override
    public String toString() {
        return "PayResultEntity{" +
                "serial_no_erp='" + serial_no_erp + '\'' +
                ", sys_code='" + sys_code + '\'' +
                ", erp_bill_no='" + erp_bill_no + '\'' +
                ", voucher_stat='" + voucher_stat + '\'' +
                ", returnBack_msg='" + returnBack_msg + '\'' +
                ", payer_acc='" + payer_acc + '\'' +
                ", payer_accname='" + payer_accname + '\'' +
                ", payer_bank='" + payer_bank + '\'' +
                ", payee_acc='" + payee_acc + '\'' +
                ", payee_accname='" + payee_accname + '\'' +
                ", payee_bank='" + payee_bank + '\'' +
                ", amt='" + amt + '\'' +
                ", purpose='" + purpose + '\'' +
                ", trans_time='" + trans_time + '\'' +
                ", return_msg='" + return_msg + '\'' +
                ", offline_date='" + offline_date + '\'' +
                ", enter_date='" + enter_date + '\'' +
                ", work_flow_id='" + work_flow_id + '\'' +
                '}';
    }
}
