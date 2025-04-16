package p8z9.tmc.bei.BTSyncController.tools;


import com.drew.lang.annotations.NotNull;
import kd.bos.openapi.common.custom.annotation.ApiModel;
import kd.bos.openapi.common.custom.annotation.ApiParam;

import java.io.Serializable;

@ApiModel
public class TransDetail implements Serializable {

    @ApiParam(value = "serial_id", required = true, message = "入参【serial_id】为空")
    @NotNull
    private String serial_id;
    @ApiParam(value = "trans_time", required = true, message = "入参【trans_time】为空")
    @NotNull
    private String trans_time;
    @ApiParam(value = "acc_name", required = true, message = "入参【acc_name】为空")
    @NotNull
    private String acc_name;
    @ApiParam(value = "bank_acc", required = true, message = "入参【bank_acc】为空")
    @NotNull
    private String bank_acc;
    @ApiParam(value = "bank_name", required = true, message = "入参【bank_name】为空")
    @NotNull
    private String bank_name;
    @ApiParam(value = "amt", required = true, message = "入参【amt】为空")
    @NotNull
    private String amt;

    @ApiParam(value = "bal",required = false, message = "入参【bal】为空")
    private String bal;
    @ApiParam(value = "cd_sign", required = true, message = "入参【cd_sign】为空")
    @NotNull
    private String cd_sign;
    @ApiParam(value = "postscript",required = false, message = "入参【postscript】为空")
    private String postscript;
    @ApiParam(value = "uses",required = false, message = "入参【uses】为空")
    private String uses;
    @ApiParam(value = "abs",required = false, message = "入参【abs】为空")
    private String abs;
    @ApiParam(value = "opp_acc_name",required = false, message = "入参【opp_acc_name】为空")
    private String opp_acc_name;
    @ApiParam(value = "opp_acc_no",required = false, message = "入参【opp_acc_no】为空")
    private String opp_acc_no;
    @ApiParam(value = "opp_acc_bank",required = false, message = "入参【opp_acc_bank】为空")
    private String opp_acc_bank;
    @ApiParam(value = "rec_time", required = false, message = "入参【rec_time】为空")
    private String rec_time;

    public String getSerial_id() {
        return serial_id;
    }

    public void setSerial_id(String serial_id) {
        this.serial_id = serial_id;
    }

    public String getTrans_time() {
        return trans_time;
    }

    public void setTrans_time(String trans_time) {
        this.trans_time = trans_time;
    }

    public String getAcc_name() {
        return acc_name;
    }

    public void setAcc_name(String acc_name) {
        this.acc_name = acc_name;
    }

    public String getBank_acc() {
        return bank_acc;
    }

    public void setBank_acc(String bank_acc) {
        this.bank_acc = bank_acc;
    }

    public String getBank_name() {
        return bank_name;
    }

    public void setBank_name(String bank_name) {
        this.bank_name = bank_name;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String getBal() {
        return bal;
    }

    public void setBal(String bal) {
        this.bal = bal;
    }

    public String getCd_sign() {
        return cd_sign;
    }

    public void setCd_sign(String cd_sign) {
        this.cd_sign = cd_sign;
    }

    public String getPostscript() {
        return postscript;
    }

    public void setPostscript(String postscript) {
        this.postscript = postscript;
    }

    public String getUses() {
        return uses;
    }

    public void setUses(String uses) {
        this.uses = uses;
    }

    public String getAbs() {
        return abs;
    }

    public void setAbs(String abs) {
        this.abs = abs;
    }

    public String getOpp_acc_name() {
        return opp_acc_name;
    }

    public void setOpp_acc_name(String opp_acc_name) {
        this.opp_acc_name = opp_acc_name;
    }

    public String getOpp_acc_no() {
        return opp_acc_no;
    }

    public void setOpp_acc_no(String opp_acc_no) {
        this.opp_acc_no = opp_acc_no;
    }

    public String getOpp_acc_bank() {
        return opp_acc_bank;
    }

    public void setOpp_acc_bank(String opp_acc_bank) {
        this.opp_acc_bank = opp_acc_bank;
    }

    public String getRec_time() {
        return rec_time;
    }

    public void setRec_time(String rec_time) {
        this.rec_time = rec_time;
    }


    @Override
    public String toString() {
        return "TransDetail{" +
                "serial_id='" + serial_id + '\'' +
                ", trans_time='" + trans_time + '\'' +
                ", acc_name='" + acc_name + '\'' +
                ", bank_acc='" + bank_acc + '\'' +
                ", bank_name='" + bank_name + '\'' +
                ", amt='" + amt + '\'' +
                ", bal='" + bal + '\'' +
                ", cd_sign='" + cd_sign + '\'' +
                ", postscript='" + postscript + '\'' +
                ", uses='" + uses + '\'' +
                ", abs='" + abs + '\'' +
                ", opp_acc_name='" + opp_acc_name + '\'' +
                ", opp_acc_no='" + opp_acc_no + '\'' +
                ", opp_acc_bank='" + opp_acc_bank + '\'' +
                ", rec_time='" + rec_time + '\'' +
                '}';
    }
}
