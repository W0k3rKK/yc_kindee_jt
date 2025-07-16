package p8z9.jtuat.tmc.cfm.webapi.dto.sealdragresult;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

import java.util.List;

/**
 * 获取印章拖拽数据结果响应 DTO
 * 对应接口规范 2.4.3 印章拖拽结果的输出参数 (isData=1)
 */
public class GetSealDragDataResultResponse extends BaseResponse {

    @JsonProperty("data")
    private List<SealDragResultData> data; // 拖拽结果数据列表

    // Getters and Setters
    public List<SealDragResultData> getData() {
        return data;
    }

    public void setData(List<SealDragResultData> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "GetSealDragDataResultResponse{" +
                "data=" + data +
                '}';
    }
}