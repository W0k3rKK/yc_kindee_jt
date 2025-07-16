package p8z9.jtuat.tmc.cfm.webapi.dto.task.filestream;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

// 印章信息DTO，用于定义 sealInfo 中的 stamps 数组元素
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StampInfo {

    // 盖章页码, 必填
    @JsonProperty("pageNo")
    private Integer pageNo;

    // 印章编号, 必填
    @JsonProperty("sealNo")
    private String sealNo;

    // 定位关键字
    @JsonProperty("keyWord")
    private String keyWord;

    // 定位坐标, 格式 "x,y"，如 "120,205"
    @JsonProperty("docPoint")
    private String docPoint;

    // 盖章次数, 范围 [1-99]
    @JsonProperty("times")
    private Integer times;

    // Getters and Setters
    public Integer getPageNo() {
        return pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public String getSealNo() {
        return sealNo;
    }

    public void setSealNo(String sealNo) {
        this.sealNo = sealNo;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getDocPoint() {
        return docPoint;
    }

    public void setDocPoint(String docPoint) {
        this.docPoint = docPoint;
    }

    public Integer getTimes() {
        return times;
    }

    public void setTimes(Integer times) {
        this.times = times;
    }

    @Override
    public String toString() {
        return "StampInfo{" +
                "pageNo=" + pageNo +
                ", sealNo='" + sealNo + '\'' +
                ", keyWord='" + keyWord + '\'' +
                ", docPoint='" + docPoint + '\'' +
                ", times=" + times +
                '}';
    }
}