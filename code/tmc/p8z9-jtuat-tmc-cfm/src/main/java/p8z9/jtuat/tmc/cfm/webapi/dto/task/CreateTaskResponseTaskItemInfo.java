package p8z9.jtuat.tmc.cfm.webapi.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 已创建任务项的信息 DTO
 * 对应接口规范 2.3.1.5 taskList中的元素
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // 可选字段如果为null则不序列化
public class CreateTaskResponseTaskItemInfo {

    @JsonProperty("fileId")
    private Integer fileId; // 任务编号 (注意：接口文档中为 "任务编号"，类型 Int MAX(11)) [cite: 166]

    @JsonProperty("fileName")
    private String fileName; // 文件名称 [cite: 166]

    @JsonProperty("scanFileId")
    private Integer scanFileId; // 回扫任务编号 (可选) [cite: 166]

    @JsonProperty("archiveFileId")
    private Integer archiveFileId; // 归档任务编号 (可选) [cite: 166]

    // Getters and Setters
    public Integer getFileId() { return fileId; }
    public void setFileId(Integer fileId) { this.fileId = fileId; }
    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Integer getScanFileId() { return scanFileId; }
    public void setScanFileId(Integer scanFileId) { this.scanFileId = scanFileId; }
    public Integer getArchiveFileId() { return archiveFileId; }
    public void setArchiveFileId(Integer archiveFileId) { this.archiveFileId = archiveFileId; }

    @Override
    public String toString() {
        return "CreatedTaskItemInfo{" +
                "fileId=" + fileId +
                ", fileName='" + fileName + '\'' +
                ", scanFileId=" + scanFileId +
                ", archiveFileId=" + archiveFileId +
                '}';
    }
}