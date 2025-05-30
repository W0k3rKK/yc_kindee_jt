package p8z9.jtuat.tmc.cfm.webapi.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 删除用户请求DTO
 * 对应接口规范 2.1.4 删除用户
 */
public class DeleteUserRequest {

    @JsonProperty("userIds")
    private List<String> userIds; // 用户ID列表

    // Getters and Setters
    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }
} 