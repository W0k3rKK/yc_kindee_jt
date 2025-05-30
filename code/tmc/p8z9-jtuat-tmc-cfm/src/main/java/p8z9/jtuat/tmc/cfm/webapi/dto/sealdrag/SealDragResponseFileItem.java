package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 印章拖拽响应中的文件项 DTO
 * 对应接口规范 2.4.1.5 fileList中的元素
 */
public class SealDragResponseFileItem {

    @JsonProperty("fileId")
    private Integer fileId; // 任务编号 (注意：接口文档中为 "任务编号"，类型 Int MAX(11)) [cite: 255]

    @JsonProperty("fileName")
    private String fileName; // 文件名称 [cite: 255]

    @JsonProperty("extFileId")
    private String extFileId; // 外围系统的文件ID (可选) [cite: 255]

    // Getters and Setters
    public Integer getFileId() { return fileId; }
    public void setFileId(Integer fileId) { this.fileId = fileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public String getExtFileId() { return extFileId; }
    public void setExtFileId(String extFileId) { this.extFileId = extFileId; }
}