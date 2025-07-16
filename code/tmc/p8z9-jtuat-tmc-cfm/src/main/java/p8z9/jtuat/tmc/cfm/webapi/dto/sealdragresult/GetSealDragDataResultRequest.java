package p8z9.jtuat.tmc.cfm.webapi.dto.sealdragresult;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// 获取印章拖拽结果的统一请求DTO
// 对应接口规范 2.4.3
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetSealDragDataResultRequest {

    // 业务流水号, 必填
    @JsonProperty(value = "tradeNo", required = true)
    private String tradeNo;

    // 盖章位置数据标志, 必填. 0:盖章位置效果URL, 1:盖章位置数据
    @JsonProperty(value = "isData", required = true)
    private Integer isData;

    // 任务编号, isData为0时可选
    @JsonProperty(value = "fileId")
    private Integer fileId;

    // 文件ID (外围系统), isData为0时可选
    @JsonProperty(value = "extFileId")
    private String extFileId;

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

    public Integer getFileId() {
        return fileId;
    }

    public void setFileId(Integer fileId) {
        this.fileId = fileId;
    }

    public String getExtFileId() {
        return extFileId;
    }

    public void setExtFileId(String extFileId) {
        this.extFileId = extFileId;
    }

    @Override
    public String toString() {
        return "GetSealDragResultRequest{" +
                "tradeNo='" + tradeNo + '\'' +
                ", isData=" + isData +
                ", fileId=" + fileId +
                ", extFileId='" + extFileId + '\'' +
                '}';
    }
}