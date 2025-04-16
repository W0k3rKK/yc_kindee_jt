package p8z9.fi.p8z9_cas_ext.PaymentBillDataSync.tool;

import com.drew.lang.annotations.NotNull;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 付款单同步实体
 */
public class PaymentBillInfoEntity implements Serializable {


    @ApiParam(value = "sys_code", required = true, message = "入参【sys_code】为空")
    @NotNull
    private String sys_code;
    @ApiParam(value = "serial_no_erp", required = true, message = "入参【serial_no_erp】为空")
    @NotNull
    private String serial_no_erp;
    @ApiParam(value = "pay_code", required = true, message = "入参【pay_code】为空")
    @NotNull
    private String pay_code;
    @ApiParam(value = "erp_bill_no", required = true, message = "入参【erp_bill_no】为空")
    @NotNull
    private String erp_bill_no;
    @ApiParam(value = "payer_acc", required = false, message = "入参【payer_acc】为空")
    private String payer_acc;
    @ApiParam(value = "payer_accname", required = false, message = "入参【payer_accname】为空")
    private String payer_accname;
    @ApiParam(value = "payer_bank", required = false, message = "入参【payer_bank】为空")
    private String payer_bank;
    @ApiParam(value = "payee_acc", required = true, message = "入参【payee_acc】为空")
    @NotNull
    private String payee_acc;
    @ApiParam(value = "payee_accname", required = true, message = "入参【payee_accname】为空")
    @NotNull
    private String payee_accname;
    @ApiParam(value = "payee_bank", required = true, message = "入参【payee_bank】为空")
    @NotNull
    private String payee_bank;
    @ApiParam(value = "payee_bank_code", required = false, message = "入参【payee_bank_code】为空")
    private String payee_bank_code;
    @ApiParam(value = "payee_addr_province", required = false, message = "入参【payee_addr_province】为空")
    private String payee_addr_province;
    @ApiParam(value = "payee_addr_city", required = false, message = "入参【payee_addr_city】为空")
    private String payee_addr_city;
    @ApiParam(value = "amt", required = true, message = "入参【amt】为空")
    @NotNull
    private BigDecimal amt;
    @ApiParam(value = "cur_code", required = true, message = "入参【cur_code】为空")
    @NotNull
    private String cur_code;
    @ApiParam(value = "purpose", required = true, message = "入参【purpose】为空")
    @NotNull
    private String purpose;
    @ApiParam(value = "wish_payday", required = true, message = "入参【wish_payday】为空")
    @NotNull
    private String wish_payday;
    @ApiParam(value = "pay_type", required = true, message = "入参【pay_type】为空")
    @NotNull
    private String pay_type;
    @ApiParam(value = "bus_type1", required = true, message = "入参【bus_type1】为空")
    @NotNull
    private String bus_type1;
    @ApiParam(value = "bus_money1", required = true, message = "入参【bus_money1】为空")
    @NotNull
    private String bus_money1;
    @ApiParam(value = "bus_type2", required = false, message = "入参【bus_type2】为空")
    private String bus_type2;
    @ApiParam(value = "bus_money2", required = false, message = "入参【bus_money2】为空")
    private String bus_money2;
    @ApiParam(value = "add_word", required = false, message = "入参【add_word】为空")
    private String add_word;

    @ApiParam(value = "remark", required = false, message = "入参【remark】为空")
    private String remark;
    @ApiParam(value = "agency_corp", required = false, message = "入参【agency_corp】为空")
    private String agency_corp;
    @ApiParam(value = "work_flow_id", required = false, message = "入参【work_flow_id】为空")
    private String work_flow_id;


    public String getSys_code() {
        return sys_code;
    }

    public void setSys_code(String sys_code) {
        this.sys_code = sys_code;
    }

    public String getSerial_no_erp() {
        return serial_no_erp;
    }

    public void setSerial_no_erp(String serial_no_erp) {
        this.serial_no_erp = serial_no_erp;
    }

    public String getPay_code() {
        return pay_code;
    }

    public void setPay_code(String pay_code) {
        this.pay_code = pay_code;
    }

    public String getErp_bill_no() {
        return erp_bill_no;
    }

    public void setErp_bill_no(String erp_bill_no) {
        this.erp_bill_no = erp_bill_no;
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

    public String getPayee_bank_code() {
        return payee_bank_code;
    }

    public void setPayee_bank_code(String payee_bank_code) {
        this.payee_bank_code = payee_bank_code;
    }

    public String getPayee_addr_province() {
        return payee_addr_province;
    }

    public void setPayee_addr_province(String payee_addr_province) {
        this.payee_addr_province = payee_addr_province;
    }

    public String getPayee_addr_city() {
        return payee_addr_city;
    }

    public void setPayee_addr_city(String payee_addr_city) {
        this.payee_addr_city = payee_addr_city;
    }

    public BigDecimal getAmt() {
        return amt;
    }

    public void setAmt(BigDecimal amt) {
        this.amt = amt;
    }

    public String getCur_code() {
        return cur_code;
    }

    public void setCur_code(String cur_code) {
        this.cur_code = cur_code;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getWish_payday() {
        return wish_payday;
    }

    public void setWish_payday(String wish_payday) {
        this.wish_payday = wish_payday;
    }

    public String getPay_type() {
        return pay_type;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public String getBus_type1() {
        return bus_type1;
    }

    public void setBus_type1(String bus_type1) {
        this.bus_type1 = bus_type1;
    }

    public String getBus_money1() {
        return bus_money1;
    }

    public void setBus_money1(String bus_money1) {
        this.bus_money1 = bus_money1;
    }

    public String getBus_type2() {
        return bus_type2;
    }

    public void setBus_type2(String bus_type2) {
        this.bus_type2 = bus_type2;
    }

    public String getBus_money2() {
        return bus_money2;
    }

    public void setBus_money2(String bus_money2) {
        this.bus_money2 = bus_money2;
    }

    public String getAdd_word() {
        return add_word;
    }

    public void setAdd_word(String add_word) {
        this.add_word = add_word;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getAgency_corp() {
        return agency_corp;
    }

    public void setAgency_corp(String agency_corp) {
        this.agency_corp = agency_corp;
    }

    public String getWork_flow_id() {
        return work_flow_id;
    }

    public void setWork_flow_id(String work_flow_id) {
        this.work_flow_id = work_flow_id;
    }

    @Override
    public String toString() {
        return "PaymentBillInfoEntity{" +
                "sys_code='" + sys_code + '\'' +
                ", serial_no_erp='" + serial_no_erp + '\'' +
                ", pay_code='" + pay_code + '\'' +
                ", erp_bill_no='" + erp_bill_no + '\'' +
                ", payer_acc='" + payer_acc + '\'' +
                ", payer_accname='" + payer_accname + '\'' +
                ", payer_bank='" + payer_bank + '\'' +
                ", payee_acc='" + payee_acc + '\'' +
                ", payee_accname='" + payee_accname + '\'' +
                ", payee_bank='" + payee_bank + '\'' +
                ", payee_bank_code='" + payee_bank_code + '\'' +
                ", payee_addr_province='" + payee_addr_province + '\'' +
                ", payee_addr_city='" + payee_addr_city + '\'' +
                ", amt=" + amt +
                ", cur_code='" + cur_code + '\'' +
                ", purpose='" + purpose + '\'' +
                ", wish_payday='" + wish_payday + '\'' +
                ", pay_type='" + pay_type + '\'' +
                ", bus_type1='" + bus_type1 + '\'' +
                ", bus_money1='" + bus_money1 + '\'' +
                ", bus_type2='" + bus_type2 + '\'' +
                ", bus_money2='" + bus_money2 + '\'' +
                ", add_word='" + add_word + '\'' +
                ", remark='" + remark + '\'' +
                ", agency_corp='" + agency_corp + '\'' +
                ", work_flow_id='" + work_flow_id + '\'' +
                '}';
    }
}
