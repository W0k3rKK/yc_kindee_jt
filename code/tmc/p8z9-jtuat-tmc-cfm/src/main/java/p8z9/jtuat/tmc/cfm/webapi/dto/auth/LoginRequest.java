package p8z9.jtuat.tmc.cfm.webapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 登录请求DTO
 * 对应接口规范 2.1.1 登录获取身份令牌
 */
public class LoginRequest {

    @JsonProperty("userId")
    private String userId; // 用户ID

    @JsonProperty("passWord")
    private String passWord; // 密码(MD5加密)

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }
} 