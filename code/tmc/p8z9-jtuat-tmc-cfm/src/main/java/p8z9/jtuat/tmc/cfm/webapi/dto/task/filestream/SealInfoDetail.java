package p8z9.jtuat.tmc.cfm.webapi.dto.task.filestream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

// 用印信息详情DTO，用于构建 sealInfo 参数的JSON结构
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SealInfoDetail {

    // 用印类型, 必填. 1:第一签发, 2:第二签发
    @JsonProperty(value = "sealType", required = true)
    private Integer sealType;

    // 印章信息, json格式字符串 必填
    @JsonProperty(value = "stamps", required = true)
    private List<StampInfo> stamps;

    // 是否对方用印
    @JsonProperty("isOtherPrint")
    private Integer isOtherPrint;

    // 打印方式
    @JsonProperty("printType")
    private Integer printType;

    // 打印颜色, 1:黑白, 2:彩色
    @JsonProperty("printColor")
    private Integer printColor;

    // 水印, 0:否, 1:是
    @JsonProperty("isWaterMark")
    private Integer isWaterMark;

    // 二维码, 0:否, 1:是
    @JsonProperty("isQrCode")
    private Integer isQrCode;

    // 骑缝章, 0:否, 1:是
    @JsonProperty("isCrossPageSeal")
    private Integer isCrossPageSeal;

    // Getters and Setters
    public Integer getSealType() {
        return sealType;
    }

    public void setSealType(Integer sealType) {
        this.sealType = sealType;
    }

    public List<StampInfo> getStamps() {
        return stamps;
    }

    public void setStamps(List<StampInfo> stamps) {
        this.stamps = stamps;
    }

    public Integer getIsOtherPrint() {
        return isOtherPrint;
    }

    public void setIsOtherPrint(Integer isOtherPrint) {
        this.isOtherPrint = isOtherPrint;
    }

    public Integer getPrintType() {
        return printType;
    }

    public void setPrintType(Integer printType) {
        this.printType = printType;
    }

    public Integer getPrintColor() {
        return printColor;
    }

    public void setPrintColor(Integer printColor) {
        this.printColor = printColor;
    }

    public Integer getIsWaterMark() {
        return isWaterMark;
    }

    public void setIsWaterMark(Integer isWaterMark) {
        this.isWaterMark = isWaterMark;
    }

    public Integer getIsQrCode() {
        return isQrCode;
    }

    public void setIsQrCode(Integer isQrCode) {
        this.isQrCode = isQrCode;
    }

    public Integer getIsCrossPageSeal() {
        return isCrossPageSeal;
    }

    public void setIsCrossPageSeal(Integer isCrossPageSeal) {
        this.isCrossPageSeal = isCrossPageSeal;
    }

    @Override
    public String toString() {
        return "SealInfoDetail{" +
                "sealType=" + sealType +
                ", stamps=" + stamps +
                ", isOtherPrint=" + isOtherPrint +
                ", printType=" + printType +
                ", printColor=" + printColor +
                ", isWaterMark=" + isWaterMark +
                ", isQrCode=" + isQrCode +
                ", isCrossPageSeal=" + isCrossPageSeal +
                '}';
    }
}