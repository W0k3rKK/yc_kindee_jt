package p8z9.jtuat.tmc.cfm.webapi.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.Code;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.seal.SealEnableRequest;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

/**
 * 印章相关API服务
 */
public class SealService {

    private static final Log log = LogFactory.get(SealService.class);
    
    private final ApiClient apiClient;
    private final String ENABLE_SEAL_ENDPOINT = "/sealFix/enableSeal"; // 对应 2.4.1

    /**
     * 构造函数
     * @param apiClient API客户端实例
     */
    public SealService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 调用"启用/停用印章"API
     * 
     * @param request 启用/停用印章请求
     * @return 基础响应
     * @throws ApiException API调用异常
     */
    public BaseResponse enableSeal(SealEnableRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("启用/停用印章请求参数不能为空");
        }
        
        if (StrUtil.isBlank(request.getSealId())) {
            throw new ApiException("印章ID不能为空");
        }
        
        if (request.getSealStatus() == null) {
            throw new ApiException("印章状态不能为空");
        }
        
        String statusDesc = request.getSealStatus() == 1 ? "启用" : "停用";
        log.info("正在调用{}印章接口，印章ID: {}", statusDesc, request.getSealId());
        
        // 发送请求
        BaseResponse response = apiClient.doPost(ENABLE_SEAL_ENDPOINT, request, BaseResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == Code.SUCCESS.getCode()) {
                log.info("{}印章成功，印章ID: {}", statusDesc, request.getSealId());
            } else {
                log.error("{}印章失败: code={}, message={}", statusDesc, response.getCode(), response.getMessage());
            }
        } else {
            log.error("{}印章响应为空", statusDesc);
        }
        
        return response;
    }
} 