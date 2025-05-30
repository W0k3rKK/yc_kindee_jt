package p8z9.jtuat.tmc.cfm.webapi.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

import java.util.List;

/**
 * 创建任务的响应体 DTO
 * 对应接口规范 2.3.1.5 / 2.3.2.5 / 2.3.3.5 输出参数
 */
public class CreateTaskResponse extends BaseResponse {

    @JsonProperty("data")
    private CreateTaskData data;

    // Getters and Setters
    public CreateTaskData getData() {
        return data;
    }

    public void setData(CreateTaskData data) {
        this.data = data;
    }
}