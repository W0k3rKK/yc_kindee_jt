package p8z9.jtuat.tmc.cfm.webapi.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CreateTaskResponseData {

    @JsonProperty("taskId")
    private String taskId; // 整体任务ID (注意：接口文档中为 "任务ID"，类型 String MAX(11)) [cite: 166]

    @JsonProperty("taskList")
    private List<CreateTaskResponseTaskItemInfo> taskList; // 创建的子任务列表 [cite: 166]

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public List<CreateTaskResponseTaskItemInfo> getTaskList() { return taskList; }
    public void setTaskList(List<CreateTaskResponseTaskItemInfo> taskList) { this.taskList = taskList; }

    @Override
    public String toString() {
        return "CreateTaskData{" +
                "taskId='" + taskId + '\'' +
                ", taskList=" + taskList +
                '}';
    }
}