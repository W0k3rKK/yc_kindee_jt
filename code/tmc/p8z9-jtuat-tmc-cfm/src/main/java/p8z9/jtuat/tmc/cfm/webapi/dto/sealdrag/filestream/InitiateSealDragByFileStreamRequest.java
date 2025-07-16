package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.filestream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.List;

/**
 * 发起印章拖拽（文件流）请求 DTO
 * 对应接口规范 2.4.1 印章拖拽(文件流)的输入参数
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略null字段
public class InitiateSealDragByFileStreamRequest {

    @JsonProperty(value = "tradeNo", required = true)
    private String tradeNo; // 业务流水号，必填 最长32位

    @JsonProperty(value = "sealFiles", required = true)
    private List<File> sealFiles; // 文件数组，必填 可传入最多 6 个文件，单个文件最大30M

    @JsonProperty(value = "taskType", required = true)
    private String taskType; // 任务类型，"01":打印用印, "05":回扫用印 [cite: 251]

    @JsonProperty(value = "devId", required = true)
    private String devId;    // 设备编号 [cite: 251]

    @JsonProperty("sealNos")
    private String sealNos;  // 印章编号集合，逗号分隔 (可选) [cite: 251]

    @JsonProperty("notifyUrl")
    private String notifyUrl; // 接收异步通知地址 (可选) [cite: 251]

    @JsonProperty("returnUrl")
    private String returnUrl; // 点选成功后跳转的网页地址 (可选) [cite: 251]

    @JsonProperty("sealDesc")
    private String sealDesc;  // 用印描述 (可选)

    @JsonProperty(value = "isCrossPageSeal", required = true, defaultValue = "0")
    private Integer isCrossPageSeal; // 是否拖拽骑缝章位置 (0否1是)，默认0，必填 [cite: 251]

    @JsonProperty("crossPageSealNos")
    private String crossPageSealNos; // 骑缝印章编号集合，逗号分隔 (可选) [cite: 251]

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public List<File> getSealFiles() {
        return sealFiles;
    }

    public void setSealFiles(List<File> sealFiles) {
        this.sealFiles = sealFiles;
    }

    public String getTaskType() {
        return taskType;
    }

    public void setTaskType(String taskType) {
        this.taskType = taskType;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getSealNos() {
        return sealNos;
    }

    public void setSealNos(String sealNos) {
        this.sealNos = sealNos;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getSealDesc() {
        return sealDesc;
    }

    public void setSealDesc(String sealDesc) {
        this.sealDesc = sealDesc;
    }

    public Integer getIsCrossPageSeal() {
        return isCrossPageSeal;
    }

    public void setIsCrossPageSeal(Integer isCrossPageSeal) {
        this.isCrossPageSeal = isCrossPageSeal;
    }

    public String getCrossPageSealNos() {
        return crossPageSealNos;
    }

    public void setCrossPageSealNos(String crossPageSealNos) {
        this.crossPageSealNos = crossPageSealNos;
    }

    @Override
    public String toString() {
        return "InitiateSealDragByFileStreamRequest{" +
                "tradeNo='" + tradeNo + '\'' +
                ", sealFiles=" + sealFiles +
                ", taskType='" + taskType + '\'' +
                ", devId='" + devId + '\'' +
                ", sealNos='" + sealNos + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                ", sealDesc='" + sealDesc + '\'' +
                ", isCrossPageSeal=" + isCrossPageSeal +
                ", crossPageSealNos='" + crossPageSealNos + '\'' +
                '}';
    }
}