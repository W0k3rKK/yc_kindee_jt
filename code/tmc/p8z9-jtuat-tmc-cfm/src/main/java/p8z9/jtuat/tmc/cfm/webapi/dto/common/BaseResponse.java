package p8z9.jtuat.tmc.cfm.webapi.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 通用API响应基类
 */
public class BaseResponse {

    @JsonProperty("code")
    private int code; // 返回码，0通常表示成功

    @JsonProperty("message")
    private String message; // 返回码描述

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}