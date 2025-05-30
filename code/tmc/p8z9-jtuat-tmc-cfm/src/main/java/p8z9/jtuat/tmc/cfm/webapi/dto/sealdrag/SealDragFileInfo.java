package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 印章拖拽文件信息 DTO
 * 对应接口规范 2.4.2 印章拖拽(文件ID)的输入参数中的sealFiles元素
 */
public class SealDragFileInfo {

    @JsonProperty("extFileId")
    private String extFileId; // 外围系统的文件ID或文件下载地址

    @JsonProperty("fileName")
    private String fileName; // 文件名称

    // Getters and Setters
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
} 