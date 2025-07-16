package p8z9.jtuat.tmc.cfm.webapi.dto.task.filestream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.File;
import java.util.List;

/**
 * 2.3.1 任务创建(文件流)接口的输入参数 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateTaskByFileStreamRequest {

    // 业务流水号, 必填
    @JsonProperty(value = "tradeNo", required = true)
    private String tradeNo;

    // 业务标题, 必填
    @JsonProperty(value = "tradeTitle", required = true)
    private String tradeTitle;

    // 任务类型, 必填. 例如 "01": 打印用印
    @JsonProperty(value = "taskType", required = true)
    private String taskType;

    // 设备编号, 必填
    @JsonProperty(value = "devId", required = true)
    private String devId;

    // 份数, 必填. 表示纸张数
    @JsonProperty(value = "copies", required = true)
    private Integer copies;

    // 任务创建人, 必填
    @JsonProperty(value = "createUser", required = true)
    private String createUser;

    // 操作人员, 必填. 用印人，设备端任务操作人员（账号）
    @JsonProperty(value = "userId", required = true)
    private String userId;

    // 用印信息, 必填. 在Service层中由SealInfoDetail对象序列化而来
    private SealInfoDetail sealInfo;

    // 文件数组, 必填. 此字段为文件本身, 不参与JSON序列化
    @JsonIgnore
    private List<File> printFiles;

    // 通知地址
    @JsonProperty("notifyUrl")
    private String notifyUrl;

    // 任务是否确认, 0否1是 默认0
    @JsonProperty("isConfirm")
    private Integer isConfirm;

    // 用印描述
    @JsonProperty("sealDesc")
    private String sealDesc;

    // 合同编号
    @JsonProperty("contrNo")
    private String contrNo;

    // 流程单号
    @JsonProperty("flowNo")
    private String flowNo;

    // 纸张类型, 0:常规A4, 1:凭证, 2:套红文件
    @JsonProperty("paperType")
    private Integer paperType;

    // 用印归档, 0否1是 默认0
    @JsonProperty("isArchive")
    private Integer isArchive;

    // Getters and Setters
    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getTradeTitle() {
        return tradeTitle;
    }

    public void setTradeTitle(String tradeTitle) {
        this.tradeTitle = tradeTitle;
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

    public Integer getCopies() {
        return copies;
    }

    public void setCopies(Integer copies) {
        this.copies = copies;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public SealInfoDetail getSealInfo() {
        return sealInfo;
    }

    public void setSealInfo(SealInfoDetail sealInfo) {
        this.sealInfo = sealInfo;
    }

    public List<File> getPrintFiles() {
        return printFiles;
    }

    public void setPrintFiles(List<File> printFiles) {
        this.printFiles = printFiles;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }

    public Integer getIsConfirm() {
        return isConfirm;
    }

    public void setIsConfirm(Integer isConfirm) {
        this.isConfirm = isConfirm;
    }

    public String getSealDesc() {
        return sealDesc;
    }

    public void setSealDesc(String sealDesc) {
        this.sealDesc = sealDesc;
    }

    public String getContrNo() {
        return contrNo;
    }

    public void setContrNo(String contrNo) {
        this.contrNo = contrNo;
    }

    public String getFlowNo() {
        return flowNo;
    }

    public void setFlowNo(String flowNo) {
        this.flowNo = flowNo;
    }

    public Integer getPaperType() {
        return paperType;
    }

    public void setPaperType(Integer paperType) {
        this.paperType = paperType;
    }

    public Integer getIsArchive() {
        return isArchive;
    }

    public void setIsArchive(Integer isArchive) {
        this.isArchive = isArchive;
    }

    @Override
    public String toString() {
        return "CreateTaskByFileStreamRequest{" +
                "tradeNo='" + tradeNo + '\'' +
                ", tradeTitle='" + tradeTitle + '\'' +
                ", taskType='" + taskType + '\'' +
                ", devId='" + devId + '\'' +
                ", copies=" + copies +
                ", createUser='" + createUser + '\'' +
                ", userId='" + userId + '\'' +
                ", sealInfo=" + sealInfo +
                ", printFiles=" + printFiles +
                ", notifyUrl='" + notifyUrl + '\'' +
                ", isConfirm=" + isConfirm +
                ", sealDesc='" + sealDesc + '\'' +
                ", contrNo='" + contrNo + '\'' +
                ", flowNo='" + flowNo + '\'' +
                ", paperType=" + paperType +
                ", isArchive=" + isArchive +
                '}';
    }
}