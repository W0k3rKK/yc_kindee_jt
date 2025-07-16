package p8z9.jtuat.tmc.cfm.webapi.dto.task.fileidlist;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 用印任务中的印章信息详细DTO
 * 对应接口规范 2.3 用印任务接口中的sealInfo字段
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SealInfoDetails {

    @JsonProperty("sealType")
    private Integer sealType; // 印章类型：1-普通印章，2-骑缝章，3-多页骑缝章，必填

    @JsonProperty("sealId")
    private String sealId; // 印章ID，必填

    @JsonProperty("sealName")
    private String sealName; // 印章名称，必填

    @JsonProperty("isWaterMark")
    private Integer isWaterMark; // 是否水印：0-否，1-是，默认0

    @JsonProperty("waterMarkText")
    private String waterMarkText; // 水印文字，当isWaterMark=1时必填

    @JsonProperty("enterName")
    private String enterName; // 用于水印、骑缝章样式区分，可选

    @JsonProperty("sealPosType")
    private Integer sealPosType; // 印章定位方式：1-坐标定位，2-关键字定位，必填

    @JsonProperty("keywordPos")
    private String keywordPos; // 关键字位置：left-左，center-中，right-右

    @JsonProperty("keyword")
    private String keyword; // 关键字，当sealPosType=2时必填

    @JsonProperty("keywordOffset")
    private Integer keywordOffset; // 关键字偏移量，当sealPosType=2时必填

    @JsonProperty("pageNum")
    private Integer pageNum; // 页码，从1开始，当sealPosType=1时必填

    @JsonProperty("posX")
    private Integer posX; // X坐标，当sealPosType=1时必填

    @JsonProperty("posY")
    private Integer posY; // Y坐标，当sealPosType=1时必填

    @JsonProperty("sealDirection")
    private Integer sealDirection; // 印章方向：0-正向，1-顺时针旋转90度，2-顺时针旋转180度，3-顺时针旋转270度，默认0

    @JsonProperty("qifengPages")
    private List<Integer> qifengPages; // 骑缝章页码列表，当sealType=2时必填

    // Getters and Setters
    public Integer getSealType() {
        return sealType;
    }

    public void setSealType(Integer sealType) {
        this.sealType = sealType;
    }

    public String getSealId() {
        return sealId;
    }

    public void setSealId(String sealId) {
        this.sealId = sealId;
    }

    public String getSealName() {
        return sealName;
    }

    public void setSealName(String sealName) {
        this.sealName = sealName;
    }

    public Integer getIsWaterMark() {
        return isWaterMark;
    }

    public void setIsWaterMark(Integer isWaterMark) {
        this.isWaterMark = isWaterMark;
    }

    public String getWaterMarkText() {
        return waterMarkText;
    }

    public void setWaterMarkText(String waterMarkText) {
        this.waterMarkText = waterMarkText;
    }

    public String getEnterName() {
        return enterName;
    }

    public void setEnterName(String enterName) {
        this.enterName = enterName;
    }

    public Integer getSealPosType() {
        return sealPosType;
    }

    public void setSealPosType(Integer sealPosType) {
        this.sealPosType = sealPosType;
    }

    public String getKeywordPos() {
        return keywordPos;
    }

    public void setKeywordPos(String keywordPos) {
        this.keywordPos = keywordPos;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getKeywordOffset() {
        return keywordOffset;
    }

    public void setKeywordOffset(Integer keywordOffset) {
        this.keywordOffset = keywordOffset;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPosX() {
        return posX;
    }

    public void setPosX(Integer posX) {
        this.posX = posX;
    }

    public Integer getPosY() {
        return posY;
    }

    public void setPosY(Integer posY) {
        this.posY = posY;
    }

    public Integer getSealDirection() {
        return sealDirection;
    }

    public void setSealDirection(Integer sealDirection) {
        this.sealDirection = sealDirection;
    }

    public List<Integer> getQifengPages() {
        return qifengPages;
    }

    public void setQifengPages(List<Integer> qifengPages) {
        this.qifengPages = qifengPages;
    }

    @Override
    public String toString() {
        return "SealInfoDetails{" +
                "sealType=" + sealType +
                ", sealId='" + sealId + '\'' +
                ", sealName='" + sealName + '\'' +
                ", isWaterMark=" + isWaterMark +
                ", waterMarkText='" + waterMarkText + '\'' +
                ", enterName='" + enterName + '\'' +
                ", sealPosType=" + sealPosType +
                ", keywordPos='" + keywordPos + '\'' +
                ", keyword='" + keyword + '\'' +
                ", keywordOffset=" + keywordOffset +
                ", pageNum=" + pageNum +
                ", posX=" + posX +
                ", posY=" + posY +
                ", sealDirection=" + sealDirection +
                ", qifengPages=" + qifengPages +
                '}';
    }
}