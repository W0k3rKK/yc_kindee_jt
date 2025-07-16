package p8z9.jtuat.tmc.cfm.webapi.dto.seal;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

import java.util.List;

/**
 * 获取印章列表的响应体 DTO
 * 对应接口规范 2.2.12 印章列表的输出参数
 */
public class ListSealsResponse extends BaseResponse {

    @JsonProperty("data")
    private List<SealInfo> data; // 印章数据列表

    // Getters and Setters
    public List<SealInfo> getData() {
        return data;
    }

    public void setData(List<SealInfo> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ListSealsResponse{" +
                "data=" + data +
                '}';
    }
}