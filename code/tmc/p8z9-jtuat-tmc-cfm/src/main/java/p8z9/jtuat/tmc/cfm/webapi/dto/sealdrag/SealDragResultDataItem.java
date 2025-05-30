package p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * 印章拖拽结果数据项 DTO
 * 对应接口规范 2.4.3 印章拖拽结果的输出参数中的data元素
 */
public class SealDragResultDataItem {

    @JsonProperty("fileName")
    private String fileName; // 文件名称

    @JsonProperty("sealList")
    private List<SealDragPositionInfo> sealList; // 印章列表

    // Getters and Setters
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<SealDragPositionInfo> getSealList() {
        return sealList;
    }

    public void setSealList(List<SealDragPositionInfo> sealList) {
        this.sealList = sealList;
    }
}