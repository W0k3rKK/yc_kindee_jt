package p8z9.jtuat.tmc.cfm.webapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 修改密码请求DTO
 * 对应接口规范 2.1.2 更换身份密文
 */
public class ChangePasswordRequest {

    @JsonProperty(value = "userId", required = true)
    private String userId; // 用户ID

    @JsonProperty(value = "oldPassWord", required = true)
    private String oldPassWord; // 原密码(MD5加密)

    @JsonProperty(value = "newPassWord", required = true)
    private String newPassWord; // 新密码(MD5加密)

    @JsonProperty(value = "confirmPassWord", required = true)
    private String confirmPassWord; // 确认新密码(MD5加密)

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOldPassWord() {
        return oldPassWord;
    }

    public void setOldPassWord(String oldPassWord) {
        this.oldPassWord = oldPassWord;
    }

    public String getNewPassWord() {
        return newPassWord;
    }

    public void setNewPassWord(String newPassWord) {
        this.newPassWord = newPassWord;
    }

    public String getConfirmPassWord() {
        return confirmPassWord;
    }

    public void setConfirmPassWord(String confirmPassWord) {
        this.confirmPassWord = confirmPassWord;
    }

    @Override
    public String toString() {
        return "ChangePasswordRequest{" +
                "userId='" + userId + '\'' +
                ", oldPassWord='" + oldPassWord + '\'' +
                ", newPassWord='" + newPassWord + '\'' +
                ", confirmPassWord='" + confirmPassWord + '\'' +
                '}';
    }
}