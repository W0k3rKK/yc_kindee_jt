package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

/**
 * 发起印章拖拽（文件流）响应data & 发起印章拖拽(文件 ID) DTO
 * 对应接口规范 2.4.1 印章拖拽(文件流) & 2.4.2 印章拖拽(文件 ID) 的输出参数
 */
public class InitiateSealDragCommonResponse extends BaseResponse {

    @JsonProperty("data")
    private InitiateSealDragResponseData data; // 结果数据

    // Getters and Setters
    public InitiateSealDragResponseData getData() {
        return data;
    }

    public void setData(InitiateSealDragResponseData data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "InitiateSealDragCommonResponse{" +
                "data=" + data +
                '}';
    }
}