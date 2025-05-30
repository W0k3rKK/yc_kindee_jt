package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

/**
 * 获取印章拖拽效果URL响应 DTO
 * 对应接口规范 2.4.3 印章拖拽结果的输出参数 (isData=0)
 */
public class GetSealDragEffectUrlResponse extends BaseResponse {

    @JsonProperty("data")
    private String data; // 拖拽效果URL

    // Getters and Setters
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}