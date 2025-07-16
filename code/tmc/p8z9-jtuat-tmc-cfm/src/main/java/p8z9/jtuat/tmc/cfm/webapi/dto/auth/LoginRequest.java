package p8z9.jtuat.tmc.cfm.webapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 登录请求DTO
 * 对应接口规范 2.1.1 登录获取身份令牌
 */
public class LoginRequest {

    @JsonProperty(value = "userId", required = true)
    private String userId; // 用户ID

    @JsonProperty(value = "passWord", required = true)
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

    @Override
    public String toString() {
        return "LoginRequest{" +
                "userId='" + userId + '\'' +
                ", passWord='" + passWord + '\'' +
                '}';
    }
}