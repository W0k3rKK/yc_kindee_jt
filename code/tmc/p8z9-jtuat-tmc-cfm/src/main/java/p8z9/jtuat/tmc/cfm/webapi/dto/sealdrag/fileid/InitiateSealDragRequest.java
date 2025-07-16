package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.fileid;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * 发起印章拖拽请求 DTO
 * 对应接口规范 2.4.2 印章拖拽(文件ID)的输入参数
 */
public class InitiateSealDragRequest {

    @JsonProperty(value = "tradeNo", required = true)
    private String tradeNo; // 业务流水号，必填

    @JsonProperty("sealFiles")
    private List<SealDragFileInfo> sealFiles; // 印章拖拽文件列表，必填

    @JsonProperty("callbackUrl")
    private String callbackUrl; // 回调地址，可选

    @JsonProperty(value = "taskType", required = true)
    private String taskType; // 任务类型，"01":打印用印, "05":回扫用印 [cite: 251]

    @JsonProperty(value = "devId", required = true)
    private String devId;    // 设备编号 [cite: 251]

    @JsonProperty("sealNos")
    private String sealNos;  // 印章编号集合，逗号分隔 (可选) [cite: 251]

    @JsonProperty(value = "isCrossPageSeal", required = true, defaultValue = "0")
    private Integer isCrossPageSeal; // 是否拖拽骑缝章位置 (0否1是)，默认0，必填 [cite: 251]

    @JsonProperty("crossPageSealNos")
    private String crossPageSealNos; // 骑缝印章编号集合，逗号分隔 (可选) [cite: 251]

    @JsonProperty("notifyUrl")
    private String notifyUrl; // 接收异步通知地址 (可选) [cite: 251]

    @JsonProperty("returnUrl")
    private String returnUrl; // 点选成功后跳转的网页地址 (可选) [cite: 251]

    // Getters and Setters
    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public List<SealDragFileInfo> getSealFiles() {
        return sealFiles;
    }

    public void setSealFiles(List<SealDragFileInfo> sealFiles) {
        this.sealFiles = sealFiles;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public void setCallbackUrl(String callbackUrl) {
        this.callbackUrl = callbackUrl;
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

    @Override
    public String toString() {
        return "InitiateSealDragRequest{" +
                "tradeNo='" + tradeNo + '\'' +
                ", sealFiles=" + sealFiles +
                ", callbackUrl='" + callbackUrl + '\'' +
                ", taskType='" + taskType + '\'' +
                ", devId='" + devId + '\'' +
                ", sealNos='" + sealNos + '\'' +
                ", isCrossPageSeal=" + isCrossPageSeal +
                ", crossPageSealNos='" + crossPageSealNos + '\'' +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", returnUrl='" + returnUrl + '\'' +
                '}';
    }
}