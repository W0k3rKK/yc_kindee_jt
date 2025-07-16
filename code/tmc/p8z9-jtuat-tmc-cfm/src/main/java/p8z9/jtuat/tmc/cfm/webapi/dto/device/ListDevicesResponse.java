package p8z9.jtuat.tmc.cfm.webapi.dto.device;

import com.fasterxml.jackson.annotation.JsonProperty;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;

import java.util.List;

/**
 * 获取设备列表的响应体 DTO
 * 对应接口规范 2.2.1 设备列表的输出参数
 */
public class ListDevicesResponse extends BaseResponse {

    @JsonProperty("data")
    private List<DeviceInfo> data; // 设备数据列表

    // Getters and Setters
    public List<DeviceInfo> getData() {
        return data;
    }

    public void setData(List<DeviceInfo> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ListDevicesResponse{" +
                "data=" + data +
                '}';
    }
}