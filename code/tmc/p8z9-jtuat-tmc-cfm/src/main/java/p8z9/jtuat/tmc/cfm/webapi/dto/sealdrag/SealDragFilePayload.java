package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SealDragFilePayload {
    @JsonProperty("extFileId")
    private String extFileId; // 外围系统的文件ID或文件下载地址 [cite: 258]

    @JsonProperty("fileName")
    private String fileName;  // 用户上传的带后缀真实文件名称 [cite: 258]

    // 构造函数, Getters, Setters
    public SealDragFilePayload(String extFileId, String fileName) {
        this.extFileId = extFileId;
        this.fileName = fileName;
    }
    public String getExtFileId() { return extFileId; }
    public void setExtFileId(String extFileId) { this.extFileId = extFileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
}