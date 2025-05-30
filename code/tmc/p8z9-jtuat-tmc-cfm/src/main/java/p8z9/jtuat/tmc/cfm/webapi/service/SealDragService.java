package p8z9.jtuat.tmc.cfm.webapi.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.*;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

import java.util.List;

/**
 * 印章拖拽相关API服务
 */
public class SealDragService {

    private static final Log log = LogFactory.get(SealDragService.class);
    
    private final ApiClient apiClient;
    private final String INITIATE_SEAL_DRAG_ENDPOINT = "/sealFix/docPointSaveUrl";
    private final String GET_SEAL_DRAG_RESULT_ENDPOINT = "/sealFix/docPointResult";

    /**
     * 构造函数
     * @param apiClient API客户端实例
     */
    public SealDragService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 调用"发起印章拖拽（文件ID）"API
     * 对应接口规范 2.4.2 印章拖拽(文件ID)
     * 
     * @param request 请求参数
     * @return 发起拖拽操作的响应，包含拖拽页面URL
     * @throws ApiException API调用异常
     */
    public InitiateSealDragResponse initiateSealDragByFileId(InitiateSealDragRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("发起印章拖拽请求参数不能为空");
        }
        
        if (StrUtil.isBlank(request.getTradeNo())) {
            throw new ApiException("业务流水号不能为空");
        }
        
        if (CollUtil.isEmpty(request.getSealFiles())) {
            throw new ApiException("印章拖拽文件列表不能为空");
        }
        
        log.info("正在调用发起印章拖拽接口，业务流水号: {}", request.getTradeNo());
        
        // 发送请求
        InitiateSealDragResponse response = apiClient.doPost(INITIATE_SEAL_DRAG_ENDPOINT, request, InitiateSealDragResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == 0) {
                log.info("发起印章拖拽成功，业务流水号: {}", request.getTradeNo());
            } else {
                log.error("发起印章拖拽失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("发起印章拖拽响应为空");
        }
        
        return response;
    }

    /**
     * 调用"获取印章拖拽结果数据"API (当 isData = 1 时使用)
     * 对应接口规范 2.4.3 印章拖拽结果 (isData为1时)
     * 
     * @param request 请求参数，必须设置 isData = 1
     * @return 拖拽结果数据
     * @throws ApiException API调用异常，或当isData不为1时
     */
    public GetSealDragDataResultResponse getSealDragResultData(GetSealDragResultRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("获取印章拖拽结果请求参数不能为空");
        }
        
        if (StrUtil.isBlank(request.getTradeNo())) {
            throw new ApiException("业务流水号不能为空");
        }
        
        if (request.getIsData() == null || request.getIsData() != 1) {
            throw new IllegalArgumentException("要获取拖拽数据，isData参数必须设置为1");
        }
        
        log.info("正在调用获取印章拖拽结果数据接口，业务流水号: {}", request.getTradeNo());
        
        // 发送请求
        GetSealDragDataResultResponse response = apiClient.doPost(GET_SEAL_DRAG_RESULT_ENDPOINT, request, GetSealDragDataResultResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == 0) {
                log.info("获取印章拖拽结果数据成功，业务流水号: {}", request.getTradeNo());
            } else {
                log.error("获取印章拖拽结果数据失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("获取印章拖拽结果数据响应为空");
        }
        
        return response;
    }

    /**
     * 调用"获取印章拖拽效果URL"API (当 isData = 0 时使用)
     * 对应接口规范 2.4.3 印章拖拽结果 (isData为0时)
     * 
     * @param request 请求参数，必须设置 isData = 0
     * @return 拖拽效果URL的响应
     * @throws ApiException API调用异常，或当isData不为0时
     */
    public GetSealDragEffectUrlResponse getSealDragResultEffectUrl(GetSealDragResultRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("获取印章拖拽效果URL请求参数不能为空");
        }
        
        if (StrUtil.isBlank(request.getTradeNo())) {
            throw new ApiException("业务流水号不能为空");
        }
        
        if (request.getIsData() == null || request.getIsData() != 0) {
            throw new IllegalArgumentException("要获取拖拽效果URL，isData参数必须设置为0");
        }
        
        log.info("正在调用获取印章拖拽效果URL接口，业务流水号: {}", request.getTradeNo());
        
        // 发送请求
        GetSealDragEffectUrlResponse response = apiClient.doPost(GET_SEAL_DRAG_RESULT_ENDPOINT, request, GetSealDragEffectUrlResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == 0) {
                log.info("获取印章拖拽效果URL成功，业务流水号: {}", request.getTradeNo());
            } else {
                log.error("获取印章拖拽效果URL失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("获取印章拖拽效果URL响应为空");
        }
        
        return response;
    }
}