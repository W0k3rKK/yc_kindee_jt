package p8z9.jtuat.tmc.cfm.webapi.dto.device;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 设备信息详情 DTO
 * 对应接口规范 2.2.1.5 输出参数中的data部分
 */
public class DeviceInfo {

    @JsonProperty("devId")
    private String devId; // 设备编号 [cite: 56]

    @JsonProperty("devName")
    private String devName; // 设备名称 [cite: 56]

    @JsonProperty("devSn")
    private String devSn; // 设备序列号 [cite: 56]

    @JsonProperty("isValid")
    private Integer isValid; // 启停标志，0:停用, 1:启用 [cite: 56]

    @JsonProperty("isTray3")
    private Integer isTray3; // 第三纸盒，0:不存在, 1:存在 [cite: 56]

    @JsonProperty("orgId")
    private String orgId; // 所属机构 ID [cite: 59]

    @JsonProperty("userIds")
    private String userIds; // 印章管理员 (逗号分隔) [cite: 59]

    @JsonProperty("itUserIds")
    private String itUserIds; // 运维人员 (逗号分隔) [cite: 59]

    @JsonProperty("sealNames")
    private String sealNames; // 可用印章名称 (逗号分隔) [cite: 59]

    // Getters and Setters
    public String getDevId() { return devId; }
    public void setDevId(String devId) { this.devId = devId; }
    public String getDevName() { return devName; }
    public void setDevName(String devName) { this.devName = devName; }
    public String getDevSn() { return devSn; }
    public void setDevSn(String devSn) { this.devSn = devSn; }
    public Integer getIsValid() { return isValid; }
    public void setIsValid(Integer isValid) { this.isValid = isValid; }
    public Integer getIsTray3() { return isTray3; }
    public void setIsTray3(Integer isTray3) { this.isTray3 = isTray3; }
    public String getOrgId() { return orgId; }
    public void setOrgId(String orgId) { this.orgId = orgId; }
    public String getUserIds() { return userIds; }
    public void setUserIds(String userIds) { this.userIds = userIds; }
    public String getItUserIds() { return itUserIds; }
    public void setItUserIds(String itUserIds) { this.itUserIds = itUserIds; }
    public String getSealNames() { return sealNames; }
    public void setSealNames(String sealNames) { this.sealNames = sealNames; }

    @Override
    public String toString() {
        return "DeviceInfo{" +
                "devId='" + devId + '\'' +
                ", devName='" + devName + '\'' +
                ", devSn='" + devSn + '\'' +
                ", isValid=" + isValid +
                ", isTray3=" + isTray3 +
                ", orgId='" + orgId + '\'' +
                ", userIds='" + userIds + '\'' +
                ", itUserIds='" + itUserIds + '\'' +
                ", sealNames='" + sealNames + '\'' +
                '}';
    }
}