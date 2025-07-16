package p8z9.jtuat.tmc.cfm.webapi.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.Code;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.InitiateSealDragCommonResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.fileid.InitiateSealDragRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.filestream.InitiateSealDragByFileStreamRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdragresult.GetSealDragDataResultRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdragresult.GetSealDragDataResultResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdragresult.GetSealDragEffectUrlRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdragresult.GetSealDragEffectUrlResponse;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * 印章拖拽相关API服务
 * - 负责构建业务参数Map并调用ApiClient
 */
public class SealDragService {

    private static final Log log = LogFactory.get(SealDragService.class);

    private final ApiClient apiClient;
    private final String INITIATE_SEAL_DRAG_BY_FILE_ID_ENDPOINT = "/sealFix/docPointSaveUrl";
    private final String INITIATE_SEAL_DRAG_BY_FILE_STREAM_ENDPOINT = "/sealFix/docPointUrl";
    private final String GET_SEAL_DRAG_RESULT_ENDPOINT = "/sealFix/docPointRet";
    private final String GET_SEAL_DRAG_RESULT_URL_ENDPOINT = "/sealFix/docPointRetUrl";

    /**
     * 构造函数
     *
     * @param apiClient API客户端实例
     */
    public SealDragService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 调用"发起印章拖拽（文件ID）"API
     * 对应接口规范 2.4.2 印章拖拽(文件ID)
     * * @param request 请求参数
     *
     * @return 发起拖拽操作的响应，包含拖拽页面URL
     * @throws ApiException API调用异常
     */
    public InitiateSealDragCommonResponse initiateSealDragByFileId(InitiateSealDragRequest request) throws ApiException {
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
        InitiateSealDragCommonResponse response = apiClient.doPost(INITIATE_SEAL_DRAG_BY_FILE_ID_ENDPOINT, request, InitiateSealDragCommonResponse.class);

        // 处理响应
        if (response != null) {
            if (response.getCode() == Code.SUCCESS.getCode()) {
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
     * 调用"发起印章拖拽（文件流）"API
     * 对应接口规范 2.4.1 印章拖拽(文件流)
     *
     * @param request 请求参数
     * @return 发起拖拽操作的响应，包含拖拽页面URL
     * @throws ApiException API调用异常
     */
    public InitiateSealDragCommonResponse initiateSealDragByFileStream(InitiateSealDragByFileStreamRequest request) throws ApiException {
        // --- 校验逻辑 ---
        if (request == null) throw new ApiException("发起印章拖拽请求参数不能为空");
        if (StrUtil.isBlank(request.getTradeNo())) throw new ApiException("业务流水号不能为空");
        if (CollUtil.isEmpty(request.getSealFiles())) throw new ApiException("印章拖拽文件列表不能为空");
        if (StrUtil.isBlank(request.getSealNos())) throw new ApiException("印章编号不能为空"); // 确认 sealNos 也为必填
        if (request.getSealFiles().size() > 6) throw new IllegalArgumentException("最多可传入 6 个文件");
        for (File file : request.getSealFiles()) {
            if (file.length() > 30 * 1024 * 1024) throw new IllegalArgumentException("单个文件最大 30M");
        }

        log.info("正在调用发起印章拖拽（文件流）接口，业务流水号: {}", request.getTradeNo());

        try {
            // --- 构建表单 ---
            Map<String, Object> formData = new HashMap<>();
            formData.put("tradeNo", request.getTradeNo());
            formData.put("taskType", request.getTaskType());
            formData.put("devId", request.getDevId());

            // 【确认添加】根据cURL，添加 sealNos 字段
            formData.put("sealNos", request.getSealNos());

            // Hutool会自动处理 List<File>，为每个文件生成一个名为 sealFiles 的 part
            formData.put("sealFiles", request.getSealFiles());

            // --- 发送请求 ---
            InitiateSealDragCommonResponse response = apiClient.doPostMultipart(
                    INITIATE_SEAL_DRAG_BY_FILE_STREAM_ENDPOINT,
                    formData,
                    InitiateSealDragCommonResponse.class
            );

            // --- 处理响应 ---
            if (response != null) {
                if (response.getCode() == Code.SUCCESS.getCode()) {
                    log.info("发起印章拖拽（文件流）成功，业务流水号: {}", request.getTradeNo());
                } else {
                    log.error("发起印章拖拽（文件流）失败: code={}, message={}", response.getCode(), response.getMessage());
                }
            } else {
                log.error("发起印章拖拽（文件流）响应为空");
            }
            return response;
        } catch (ApiException e) {
            log.error("发起印章拖拽（文件流）API调用异常", e);
            throw new ApiException("发起印章拖拽（文件流）API调用异常: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("发起印章拖拽（文件流）发生未知异常", e);
            throw new ApiException("发起印章拖拽（文件流）发生未知异常: " + e.getMessage(), e);
        }
    }

    /**
     * 调用"获取印章拖拽结果数据"API (当 isData = 1 时使用)
     * 对应接口规范 2.4.3 印章拖拽结果 (isData为1时)
     * * @param request 请求参数，必须设置 isData = 1
     *
     * @return 拖拽结果数据
     * @throws ApiException API调用异常，或当isData不为1时
     */
    public GetSealDragDataResultResponse getSealDragResultData(GetSealDragDataResultRequest request) throws ApiException {
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
            if (response.getCode() == Code.SUCCESS.getCode()) {
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
     * 调用"获取印章拖拽效果URL"API
     * 对应接口规范 2.4.4 印章拖拽效果
     * * @param request 请求参数，必须设置 tradeNo 为原拖拽/创建任务的业务流水
     *
     * @return 拖拽效果URL的响应
     * @throws ApiException API调用异常
     */
    public GetSealDragEffectUrlResponse getSealDragResultEffectUrl(GetSealDragEffectUrlRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("获取印章拖拽效果URL请求参数不能为空");
        }

        if (StrUtil.isBlank(request.getTradeNo())) {
            throw new ApiException("业务流水号不能为空");
        }

        log.info("正在调用获取印章拖拽效果URL接口，业务流水号: {}", request.getTradeNo());

        // 发送请求
        GetSealDragEffectUrlResponse response = apiClient.doPost(
                GET_SEAL_DRAG_RESULT_URL_ENDPOINT,
                request,
                GetSealDragEffectUrlResponse.class);

        // 处理响应
        if (response != null) {
            if (response.getCode() == Code.SUCCESS.getCode()) {
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