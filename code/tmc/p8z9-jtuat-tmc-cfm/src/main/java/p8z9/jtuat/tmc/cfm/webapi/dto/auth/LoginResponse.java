package p8z9.jtuat.tmc.cfm.webapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

/**
 * 登录响应DTO
 * 对应接口规范 2.1.1 登录获取身份令牌
 */
public class LoginResponse extends BaseResponse {

    @JsonProperty("data")
    private String data; // 身份令牌

    // Getters and Setters
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "LoginResponse{" +
                "data='" + data + '\'' +
                '}';
    }
}