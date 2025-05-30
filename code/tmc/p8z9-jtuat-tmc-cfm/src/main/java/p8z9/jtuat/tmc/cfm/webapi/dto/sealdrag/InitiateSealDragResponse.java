package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

/**
 * 发起印章拖拽响应 DTO
 * 对应接口规范 2.4.2 印章拖拽(文件ID)的输出参数
 */
public class InitiateSealDragResponse extends BaseResponse {

    @JsonProperty("data")
    private String data; // 拖拽页面URL

    // Getters and Setters
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}