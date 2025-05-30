package p8z9.jtuat.tmc.cfm.webapi.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 同步用户的请求DTO
 * 对应接口规范 2.1.3 同步用户
 */
public class UserSaveRequest {

    @JsonProperty("orgId")
    private String orgId; // 组织ID

    @JsonProperty("userName")
    private String userName; // 用户名称

    @JsonProperty("passWord")
    private String passWord; // 用户密码(MD5加密)

    @JsonProperty("userId")
    private String userId; // 用户ID

    // Getters and Setters
    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 