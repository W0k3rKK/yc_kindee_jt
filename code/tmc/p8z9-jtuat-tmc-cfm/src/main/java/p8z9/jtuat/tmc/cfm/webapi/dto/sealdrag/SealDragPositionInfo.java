package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 印章拖拽位置信息 DTO
 * 对应接口规范 2.4.3 印章拖拽结果的输出参数中的sealList元素
 */
public class SealDragPositionInfo {

    @JsonProperty("sealId")
    private String sealId; // 印章ID

    @JsonProperty("sealName")
    private String sealName; // 印章名称

    @JsonProperty("pageNum")
    private Integer pageNum; // 页码

    @JsonProperty("posX")
    private Integer posX; // X坐标

    @JsonProperty("posY")
    private Integer posY; // Y坐标

    @JsonProperty("sealDirection")
    private Integer sealDirection; // 印章方向，0-正向，1-顺时针90度，2-180度，3-逆时针90度

    // Getters and Setters
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
} 