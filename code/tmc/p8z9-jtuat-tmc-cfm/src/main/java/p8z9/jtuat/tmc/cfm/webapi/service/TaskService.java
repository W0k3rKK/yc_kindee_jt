package p8z9.jtuat.tmc.cfm.webapi.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.Code;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.CreateTaskResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.fileidlist.CreateTaskByFileIdRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.filestream.CreateTaskByFileStreamRequest;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 任务相关API服务
 */
public class TaskService {

    private static final Log log = LogFactory.get(TaskService.class);

    private final ApiClient apiClient;
    private final String CREATE_TASK_BY_FILE_ID_ENDPOINT = "/sealFix/createTask2"; // 对应 2.3.2 任务创建(文件 ID)_列表
    private final String CREATE_TASK_BY_FILE_STREAM_ENDPOINT = "/sealFix/createTask"; // 对应 2.3.1 任务创建(文件流)

    /**
     * 构造函数
     *
     * @param apiClient API客户端实例
     */
    public TaskService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 调用"创建任务（文件ID模式）"API
     * 根据印控系统对接指引，创建任务前必须确保用印人已通过用户同步接口添加到系统中
     * 对应接口规范 2.3.2 创建任务（文件ID）
     * * @param requestList 创建任务请求列表
     *
     * @return 创建任务响应
     * @throws ApiException API调用异常
     */
    public CreateTaskResponse createTaskByFileId(List<CreateTaskByFileIdRequest> requestList) throws ApiException {
        // 参数校验
        if (requestList == null || requestList.isEmpty()) {
            throw new ApiException("创建任务请求列表不能为空");
        }

        log.info("正在调用创建任务(文件ID)接口，任务数量: {}", requestList.size());

        // 验证任务数据
        for (int i = 0; i < requestList.size(); i++) {
            CreateTaskByFileIdRequest task = requestList.get(i);

            if (StrUtil.isBlank(task.getTradeNo())) {
                throw new ApiException("第" + (i + 1) + "个任务的业务流水号不能为空");
            }

            if (StrUtil.isBlank(task.getTradeTitle())) {
                throw new ApiException("第" + (i + 1) + "个任务的业务标题不能为空");
            }

            if (StrUtil.isBlank(task.getExtFileId())) {
                throw new ApiException("第" + (i + 1) + "个任务的文件ID或下载地址不能为空");
            }

            if (StrUtil.isBlank(task.getFileName())) {
                throw new ApiException("第" + (i + 1) + "个任务的文件名称不能为空");
            }

            if (StrUtil.isBlank(task.getTaskType())) {
                throw new ApiException("第" + (i + 1) + "个任务的任务类型不能为空");
            }

            if (StrUtil.isBlank(task.getDevId())) {
                throw new ApiException("第" + (i + 1) + "个任务的设备编号不能为空");
            }

            if (StrUtil.isBlank(task.getUserId())) {
                throw new ApiException("第" + (i + 1) + "个任务的用户ID不能为空");
            }

            if (task.getSealInfo() == null) {
                throw new ApiException("第" + (i + 1) + "个任务的印章信息不能为空");
            }

            // 记录任务详情
            log.info("任务 #{} - 流水号: {}, 标题: {}, 用户: {}",
                    i + 1, task.getTradeNo(), task.getTradeTitle(), task.getUserId());
        }

        // 发送请求
        CreateTaskResponse response = apiClient.doPost(CREATE_TASK_BY_FILE_ID_ENDPOINT, requestList, CreateTaskResponse.class);

        // 处理响应
        if (response != null) {
            if (response.getCode() == Code.SUCCESS.getCode()) {
                log.info("创建任务成功，任务ID: {}",
                        (response.getData() != null ? response.getData().getTaskId() : "未知"));
            } else {
                log.error("创建任务失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("创建任务响应为空");
        }

        return response;
    }

    // ======================【新增服务方法】======================

    /**
     * 调用"创建任务（文件流模式）"API
     * 对应接口规范 2.3.1 任务创建(文件流)
     *
     * @param request 创建任务(文件流)的请求
     * @return 创建任务响应
     * @throws ApiException API调用异常
     */
    public CreateTaskResponse createTaskByFileStream(CreateTaskByFileStreamRequest request) throws ApiException {
        // --- 1. 客户端前置校验 ---
        if (request == null) throw new ApiException("请求对象不能为空");
        if (StrUtil.isBlank(request.getTradeNo())) throw new ApiException("业务流水号(tradeNo)不能为空");
        if (StrUtil.isBlank(request.getTradeTitle())) throw new ApiException("业务标题(tradeTitle)不能为空");
        if (CollUtil.isEmpty(request.getPrintFiles())) throw new ApiException("文件列表(printFiles)不能为空");
        if (StrUtil.isBlank(request.getTaskType())) throw new ApiException("任务类型(taskType)不能为空");
        if (StrUtil.isBlank(request.getDevId())) throw new ApiException("设备编号(devId)不能为空");
        if (request.getCopies() == null) throw new ApiException("份数(copies)不能为空");
        if (StrUtil.isBlank(request.getCreateUser())) throw new ApiException("任务创建人(createUser)不能为空");
        if (StrUtil.isBlank(request.getUserId())) throw new ApiException("操作人(userId)不能为空");
        if (request.getSealInfo() == null) throw new ApiException("用印信息(sealInfo)不能为空");

        log.info("正在调用创建任务(文件流)接口，业务流水号: {}, 文件数量: {}", request.getTradeNo(), request.getPrintFiles().size());

        try {
            // --- 2. 构建 multipart/form-data 表单 ---
            Map<String, Object> formData = new HashMap<>();

            // 添加普通文本字段
            formData.put("tradeNo", request.getTradeNo());
            formData.put("tradeTitle", request.getTradeTitle());
            formData.put("taskType", request.getTaskType());
            formData.put("devId", request.getDevId());
            formData.put("copies", request.getCopies());
            formData.put("createUser", request.getCreateUser());
            formData.put("userId", request.getUserId());

            // 添加可选文本字段
            if (StrUtil.isNotBlank(request.getNotifyUrl())) formData.put("notifyUrl", request.getNotifyUrl());
            if (request.getIsConfirm() != null) formData.put("isConfirm", request.getIsConfirm());
            if (StrUtil.isNotBlank(request.getSealDesc())) formData.put("sealDesc", request.getSealDesc());
            if (StrUtil.isNotBlank(request.getContrNo())) formData.put("contrNo", request.getContrNo());
            if (StrUtil.isNotBlank(request.getFlowNo())) formData.put("flowNo", request.getFlowNo());
            if (request.getPaperType() != null) formData.put("paperType", request.getPaperType());
            if (request.getIsArchive() != null) formData.put("isArchive", request.getIsArchive());

            // 【特殊处理】将 sealInfo 对象序列化为JSON字符串
            String sealInfoJson = JSONUtil.toJsonStr(request.getSealInfo());
            formData.put("sealInfo", sealInfoJson);
            log.debug("序列化后的sealInfo: {}", sealInfoJson);

            // 添加文件列表, key为"printFile", 与接口文档保持一致
            formData.put("printFile", request.getPrintFiles());

            // --- 3. 发送请求 ---
            CreateTaskResponse response = apiClient.doPostMultipart(
                    CREATE_TASK_BY_FILE_STREAM_ENDPOINT,
                    formData,
                    CreateTaskResponse.class
            );

            // --- 4. 处理响应 ---
            if (response != null) {
                if (response.getCode() == Code.SUCCESS.getCode()) {
                    log.info("创建任务(文件流)成功，任务ID: {}",
                            (response.getData() != null ? response.getData().getTaskId() : "未知"));
                } else {
                    log.error("创建任务(文件流)失败: code={}, message={}", response.getCode(), response.getMessage());
                }
            } else {
                log.error("创建任务(文件流)响应为空");
            }
            return response;

        } catch (ApiException e) {
            log.error("创建任务(文件流)API调用异常", e);
            throw e; // 直接向上抛出
        } catch (Exception e) {
            log.error("创建任务(文件流)发生未知异常", e);
            throw new ApiException("创建任务(文件流)发生未知异常: " + e.getMessage(), e);
        }
    }
}