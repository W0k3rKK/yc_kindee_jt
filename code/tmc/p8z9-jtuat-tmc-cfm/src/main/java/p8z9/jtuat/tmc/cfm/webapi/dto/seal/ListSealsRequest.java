package p8z9.jtuat.tmc.cfm.webapi.dto.seal;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 获取印章列表的请求体 DTO
 * 对应接口规范 2.2.12 印章列表的输入参数
 */
@JsonInclude(JsonInclude.Include.NON_NULL) // 序列化时忽略null字段
public class ListSealsRequest {

    @JsonProperty("devId")
    private String devId; // 设备编号 (可选)

    @JsonProperty("sealKind") // 文档中为 sealkind，Java中通常用驼峰
    private String sealKind; // 印章种类 (可选) [cite: 118]

    @JsonProperty("orgIds")
    private String orgIds;   // 所属机构ID，逗号分割 (可选) [cite: 118]

    // 构造函数
    public ListSealsRequest() {}

    // Getters and Setters
    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getSealKind() {
        return sealKind;
    }

    public void setSealKind(String sealKind) {
        this.sealKind = sealKind;
    }

    public String getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(String orgIds) {
        this.orgIds = orgIds;
    }
}