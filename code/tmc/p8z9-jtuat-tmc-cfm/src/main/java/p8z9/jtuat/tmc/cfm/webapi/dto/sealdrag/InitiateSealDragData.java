package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class InitiateSealDragData {

    @JsonProperty("taskId")
    private String taskId; // 拖拽会话的任务ID (注意：接口文档中为 "任务ID"，类型 String MAX(32)) [cite: 253]

    @JsonProperty("pointUrl")
    private String pointUrl; // 点选盖章位置的URL [cite: 255]

    @JsonProperty("fileList")
    private List<SealDragResponseFileItem> fileList; // 文件列表 [cite: 255]

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getPointUrl() { return pointUrl; }
    public void setPointUrl(String pointUrl) { this.pointUrl = pointUrl; }
    public List<SealDragResponseFileItem> getFileList() { return fileList; }
    public void setFileList(List<SealDragResponseFileItem> fileList) { this.fileList = fileList; }
}