package p8z9.jtuat.tmc.cfm.webapi.dto.device;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 获取设备列表的请求体 DTO
 * 对应接口规范 2.2.1 设备列表的输入参数
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略null字段
public class ListDevicesRequest {

    @JsonProperty("orgIds")
    private String orgIds; // 所属机构ID，逗号分割 (可选)

    @JsonProperty("isValid")
    private Integer isValid; // 启停标志，0:停用, 1:启用 (可选)

    // 构造函数
    public ListDevicesRequest() {}

    // Getters and Setters
    public String getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(String orgIds) {
        this.orgIds = orgIds;
    }

    public Integer getIsValid() {
        return isValid;
    }

    public void setIsValid(Integer isValid) {
        this.isValid = isValid;
    }

    @Override
    public String toString() {
        return "ListDevicesRequest{" +
                "orgIds='" + orgIds + '\'' +
                ", isValid=" + isValid +
                '}';
    }
}