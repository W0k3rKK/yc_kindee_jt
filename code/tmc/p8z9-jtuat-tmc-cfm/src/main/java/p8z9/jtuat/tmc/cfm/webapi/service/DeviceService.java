package p8z9.jtuat.tmc.cfm.webapi.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.Code;
import p8z9.jtuat.tmc.cfm.webapi.dto.device.ListDevicesRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.device.ListDevicesResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.seal.ListSealsRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.seal.ListSealsResponse;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

/**
 * 设备相关API服务
 */
public class DeviceService {

    private static final Log log = LogFactory.get(DeviceService.class);
    
    private final ApiClient apiClient;
    private final String LIST_DEVICES_ENDPOINT = "/sealFix/devList"; // 对应 2.2.1 设备列表
    private final String LIST_SEALS_ENDPOINT = "/sealFix/sealList";  // 对应 2.2.12 印章列表

    /**
     * 构造函数
     * @param apiClient API客户端实例
     */
    public DeviceService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 调用"获取设备列表"API
     * 对应接口规范 2.2.1 设备列表
     * 
     * @param request 请求参数
     * @return 设备列表响应
     * @throws ApiException API调用异常
     */
    public ListDevicesResponse listDevices(ListDevicesRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("获取设备列表请求参数不能为空");
        }
        
        log.info("正在调用获取设备列表接口");
        
        // 发送请求
        ListDevicesResponse response = apiClient.doPost(LIST_DEVICES_ENDPOINT, request, ListDevicesResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == Code.SUCCESS.getCode()) {
                int deviceCount = (response.getData() != null) ? response.getData().size() : 0;
                log.info("获取设备列表成功，共 {} 个设备", deviceCount);
            } else {
                log.error("获取设备列表失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("获取设备列表响应为空");
        }
        
        return response;
    }

    /**
     * 调用"获取印章列表"API
     * 对应接口规范 2.2.12 印章列表
     * 
     * @param request 请求参数，包含devId或sealKind等过滤条件
     * @return 印章列表响应
     * @throws ApiException API调用异常
     */
    public ListSealsResponse listSeals(ListSealsRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("获取印章列表请求参数不能为空");
        }
        
        // 记录设备ID信息（如果有）
        if (StrUtil.isNotBlank(request.getDevId())) {
            log.info("正在调用获取印章列表接口，设备ID: {}", request.getDevId());
        } else {
            log.info("正在调用获取印章列表接口，查询所有印章");
        }
        
        // 发送请求
        ListSealsResponse response = apiClient.doPost(LIST_SEALS_ENDPOINT, request, ListSealsResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == Code.SUCCESS.getCode()) {
                int sealCount = (response.getData() != null) ? response.getData().size() : 0;
                log.info("获取印章列表成功，共 {} 个印章", sealCount);
            } else {
                log.error("获取印章列表失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("获取印章列表响应为空");
        }
        
        return response;
    }
}