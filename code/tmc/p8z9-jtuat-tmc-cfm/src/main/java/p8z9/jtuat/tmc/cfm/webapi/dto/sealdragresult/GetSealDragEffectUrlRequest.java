package p8z9.jtuat.tmc.cfm.webapi.dto.sealdragresult;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 获取印章拖拽结果请求 DTO
 * 对应接口规范 2.4.4 印章拖拽效果的输入参数
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GetSealDragEffectUrlRequest {

    @JsonProperty(value = "tradeNo", required = true)
    private String tradeNo; // 业务流水号，必填

    @JsonProperty(value = "fileId")
    private Integer fileId; // 任务编号 (可选)

    @JsonProperty(value = "extFileId")
    private String extFileId; // 文件ID

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
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
        return "GetSealDragEffectUrlRequest{" +
                "tradeNo='" + tradeNo + '\'' +
                ", fileId=" + fileId +
                ", extFileId='" + extFileId + '\'' +
                '}';
    }
}
