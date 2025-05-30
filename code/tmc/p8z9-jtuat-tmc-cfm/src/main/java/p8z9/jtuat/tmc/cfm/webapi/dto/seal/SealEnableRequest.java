package p8z9.jtuat.tmc.cfm.webapi.dto.seal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 印章启用/停用请求DTO
 * 对应接口规范 2.2.13 启用/停用印章
 */
public class SealEnableRequest {

    @JsonProperty("sealId")
    private String sealId; // 印章ID

    @JsonProperty("sealStatus")
    private Integer sealStatus; // 印章状态：0-停用，1-启用

    // Getters and Setters
    public String getSealId() {
        return sealId;
    }

    public void setSealId(String sealId) {
        this.sealId = sealId;
    }

    public Integer getSealStatus() {
        return sealStatus;
    }

    public void setSealStatus(Integer sealStatus) {
        this.sealStatus = sealStatus;
    }
} 