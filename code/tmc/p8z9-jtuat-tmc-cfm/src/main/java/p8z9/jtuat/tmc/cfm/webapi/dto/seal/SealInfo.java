package p8z9.jtuat.tmc.cfm.webapi.dto.seal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 印章信息详情 DTO
 * 对应接口规范 2.2.12.5 输出参数中的data部分
 */
public class SealInfo {

    @JsonProperty("sealNo")
    private String sealNo; // 印章编号 [cite: 121]

    @JsonProperty("sealId") // 文档中为 sealld，通常应为 sealId，此处按文档
    private Integer sealStoreId; // 章库编号 [cite: 121]

    @JsonProperty("sealName")
    private String sealName; // 印章名称 [cite: 121]

    @JsonProperty("devId")
    private String devId; // 设备编号 [cite: 121]

    @JsonProperty("devName")
    private String devName; // 设备名称 [cite: 121]

    @JsonProperty("sealKind")
    private String sealKind; // 印章种类 [cite: 121]

    @JsonProperty("sealKindName")
    private String sealKindName; // 种类名称 [cite: 121]

    @JsonProperty("isBorrow")
    private Integer isBorrow; // 是否外借，0否1是 [cite: 121]

    @JsonProperty("sealShowImg")
    private String sealShowImg; // 印章图像 (Base64的png图片) [cite: 121]

    // Getters and Setters
    public String getSealNo() { return sealNo; }
    public void setSealNo(String sealNo) { this.sealNo = sealNo; }
    public Integer getSealStoreId() { return sealStoreId; }
    public void setSealStoreId(Integer sealStoreId) { this.sealStoreId = sealStoreId; }
    public String getSealName() { return sealName; }
    public void setSealName(String sealName) { this.sealName = sealName; }
    public String getDevId() { return devId; }
    public void setDevId(String devId) { this.devId = devId; }
    public String getDevName() { return devName; }
    public void setDevName(String devName) { this.devName = devName; }
    public String getSealKind() { return sealKind; }
    public void setSealKind(String sealKind) { this.sealKind = sealKind; }
    public String getSealKindName() { return sealKindName; }
    public void setSealKindName(String sealKindName) { this.sealKindName = sealKindName; }
    public Integer getIsBorrow() { return isBorrow; }
    public void setIsBorrow(Integer isBorrow) { this.isBorrow = isBorrow; }
    public String getSealShowImg() { return sealShowImg; }
    public void setSealShowImg(String sealShowImg) { this.sealShowImg = sealShowImg; }
}