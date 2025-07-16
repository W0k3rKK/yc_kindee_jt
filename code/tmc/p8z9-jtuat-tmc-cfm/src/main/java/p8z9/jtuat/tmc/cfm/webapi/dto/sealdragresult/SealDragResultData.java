package p8z9.jtuat.tmc.cfm.webapi.dto.sealdragresult;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// 印章拖拽结果数据DTO
// 对应接口规范 2.4.3.5 data 列表中的元素 (isData=1时)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SealDragResultData {

    // 文件编号, 必填
    @JsonProperty("fileId")
    private String fileId;

    // 文件ID (外围系统), 可选
    @JsonProperty("extFileId")
    private String extFileId;

    // 文件名称, 必填
    @JsonProperty("fileName")
    private String fileName;

    // 设备编号, 必填
    @JsonProperty("devId")
    private String devId;

    // 业务流水号, 必填
    @JsonProperty("tradeNo")
    private String tradeNo;

    // 印章位置信息, 可选. 文档描述为JSON格式字符串
    @JsonProperty("stamps")
    private String stamps;

    // Getters and Setters
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

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDevId() {
        return devId;
    }

    public void setDevId(String devId) {
        this.devId = devId;
    }

    public String getTradeNo() {
        return tradeNo;
    }

    public void setTradeNo(String tradeNo) {
        this.tradeNo = tradeNo;
    }

    public String getStamps() {
        return stamps;
    }

    public void setStamps(String stamps) {
        this.stamps = stamps;
    }

    @Override
    public String toString() {
        return "SealDragResultData{" +
                "fileId='" + fileId + '\'' +
                ", extFileId='" + extFileId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", devId='" + devId + '\'' +
                ", tradeNo='" + tradeNo + '\'' +
                ", stamps='" + stamps + '\'' +
                '}';
    }
}