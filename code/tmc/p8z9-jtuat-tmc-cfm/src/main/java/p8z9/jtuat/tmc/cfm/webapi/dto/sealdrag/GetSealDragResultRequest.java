package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 获取印章拖拽结果请求 DTO
 * 对应接口规范 2.4.3 印章拖拽结果的输入参数
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetSealDragResultRequest {

    @JsonProperty("tradeNo")
    private String tradeNo; // 业务流水号，必填

    @JsonProperty("isData")
    private Integer isData; // 是否获取数据，1-获取数据，0-获取效果URL，必填

    @JsonProperty("fileId")
    private String fileId; // 任务编号 (可选, 用于isData为0时)。注意：规范中此参数为Int，但响应中相关fileId为String，此处统一为String以简化处理，或严格按规范处理。PDF 2.4.3.4 fileId为Int。

    @JsonProperty("extFileId")
    private String extFileId; // 文件ID (外围系统文件ID, 可选, 用于isData为0时) [cite: 261]

    // 构造函数
    public GetSealDragResultRequest() {}

    public GetSealDragResultRequest(String tradeNo, Integer isData) {
        this.tradeNo = tradeNo;
        this.isData = isData;
    }

    // Getters and Setters
    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public Integer getIsData() {
        return isData;
    }

    public void setIsData(Integer isData) {
        this.isData = isData;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public String getExtFileId() {
        return extFileId;
    }

    public void setExtFileId(String extFileId) {
        this.extFileId = extFileId;
    }
}