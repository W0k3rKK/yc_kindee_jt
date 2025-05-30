package p8z9.jtuat.tmc.cfm.webapi.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 任务预创建请求 DTO
 * 对应接口规范 2.3.2 任务预创建的输入参数
 */
public class PreCreateTaskRequest {

    @JsonProperty("tradeNo")
    private String tradeNo; // 业务流水号，必填

    @JsonProperty("tradeTitle")
    private String tradeTitle; // 业务标题，必填

    @JsonProperty("extFileId")
    private String extFileId; // 外围系统的文件ID或文件下载地址，必填

    @JsonProperty("fileName")
    private String fileName; // 文件名称，必填

    @JsonProperty("taskType")
    private String taskType; // 任务类型，例如 "01" (打印用印)，必填

    @JsonProperty("devId")
    private String devId; // 设备编号，必填

    @JsonProperty("copies")
    private Integer copies; // 份数，默认1

    @JsonProperty("userId")
    private String userId; // 用印人ID，必填，必须是已通过用户同步接口添加的用户

    @JsonProperty("createUser")
    private String createUser; // 创建人，可选

    @JsonProperty("isConfirm")
    private Integer isConfirm; // 任务确认，0否1是，默认0

    @JsonProperty("notifyUrl")
    private String notifyUrl; // 异步通知地址 (可选)

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

    public String getExtFileId() {
        return extFileId;
    }

    public void setExtFileId(String extFileId) {
        this.extFileId = extFileId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
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

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public Integer getIsConfirm() {
        return isConfirm;
    }

    public void setIsConfirm(Integer isConfirm) {
        this.isConfirm = isConfirm;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public void setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
    }
} 