package p8z9.jtuat.tmc.cfm.webapi.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

/**
 * 创建任务的响应体 DTO
 * 对应接口规范 2.3.1.5 任务创建(文件流) / 2.3.2.5 任务创建(文件 ID)_列表 / 2.3.3.5 任务创建(文件 ID) 输出参数
 */
public class CreateTaskResponse extends BaseResponse {

    @JsonProperty("data")
    private CreateTaskResponseData data;

    // Getters and Setters
    public CreateTaskResponseData getData() {
        return data;
    }

    public void setData(CreateTaskResponseData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "CreateTaskResponse{" +
                "data=" + data +
                '}';
    }
}