package p8z9.jtuat.tmc.cfm.webapi.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.CreateTaskByFileIdRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.CreateTaskResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.PreCreateTaskRequest;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

import java.util.List;

/**
 * 任务相关API服务
 */
public class TaskService {

    private static final Log log = LogFactory.get(TaskService.class);
    
    private final ApiClient apiClient;
    private final String CREATE_TASK_BY_FILE_ID_ENDPOINT = "/sealFix/createTask2"; // 对应 2.3.1
    private final String PRE_CREATE_TASK_ENDPOINT = "/sealFix/preCreateTask"; // 对应 2.3.2 任务预创建

    /**
     * 构造函数
     * @param apiClient API客户端实例
     */
    public TaskService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 调用"创建任务（文件ID模式）"API
     * 根据印控系统对接指引，创建任务前必须确保用印人已通过用户同步接口添加到系统中
     * 对应接口规范 2.3.1 创建任务（文件ID）
     * 
     * @param requestList 创建任务请求列表
     * @return 创建任务响应
     * @throws ApiException API调用异常
     */
    public CreateTaskResponse createTaskByFileId(List<CreateTaskByFileIdRequest> requestList) throws ApiException {
        // 参数校验
        if (requestList == null || requestList.isEmpty()) {
            throw new ApiException("创建任务请求列表不能为空");
        }
        
        log.info("正在调用创建任务接口，任务数量: {}", requestList.size());
        
        // 验证任务数据
        for (int i = 0; i < requestList.size(); i++) {
            CreateTaskByFileIdRequest task = requestList.get(i);
            
            if (StrUtil.isBlank(task.getTradeNo())) {
                throw new ApiException("第" + (i+1) + "个任务的业务流水号不能为空");
            }
            
            if (StrUtil.isBlank(task.getTradeTitle())) {
                throw new ApiException("第" + (i+1) + "个任务的业务标题不能为空");
            }
            
            if (StrUtil.isBlank(task.getExtFileId())) {
                throw new ApiException("第" + (i+1) + "个任务的文件ID或下载地址不能为空");
            }
            
            if (StrUtil.isBlank(task.getFileName())) {
                throw new ApiException("第" + (i+1) + "个任务的文件名称不能为空");
            }
            
            if (StrUtil.isBlank(task.getTaskType())) {
                throw new ApiException("第" + (i+1) + "个任务的任务类型不能为空");
            }
            
            if (StrUtil.isBlank(task.getDevId())) {
                throw new ApiException("第" + (i+1) + "个任务的设备编号不能为空");
            }
            
            if (StrUtil.isBlank(task.getUserId())) {
                throw new ApiException("第" + (i+1) + "个任务的用户ID不能为空");
            }
            
            if (task.getSealInfo() == null) {
                throw new ApiException("第" + (i+1) + "个任务的印章信息不能为空");
            }
            
            // 记录任务详情
            log.debug("任务 #{} - 流水号: {}, 标题: {}, 用户: {}", 
                    i+1, task.getTradeNo(), task.getTradeTitle(), task.getUserId());
        }
        
        // 发送请求
        CreateTaskResponse response = apiClient.doPost(CREATE_TASK_BY_FILE_ID_ENDPOINT, requestList, CreateTaskResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == 0) {
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
    
    /**
     * 调用"任务预创建"API
     * 对应接口规范 2.3.2 任务预创建
     * 
     * @param request 任务预创建请求
     * @return 任务预创建响应
     * @throws ApiException API调用异常
     */
    public CreateTaskResponse preCreateTask(PreCreateTaskRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("任务预创建请求不能为空");
        }
        
        log.info("正在调用任务预创建接口，业务流水号: {}", request.getTradeNo());
        
        // 验证任务数据
        if (StrUtil.isBlank(request.getTradeNo())) {
            throw new ApiException("任务的业务流水号不能为空");
        }
        
        if (StrUtil.isBlank(request.getTradeTitle())) {
            throw new ApiException("任务的业务标题不能为空");
        }
        
        if (StrUtil.isBlank(request.getExtFileId())) {
            throw new ApiException("任务的文件ID或下载地址不能为空");
        }
        
        if (StrUtil.isBlank(request.getFileName())) {
            throw new ApiException("任务的文件名称不能为空");
        }
        
        if (StrUtil.isBlank(request.getTaskType())) {
            throw new ApiException("任务的任务类型不能为空");
        }
        
        if (StrUtil.isBlank(request.getDevId())) {
            throw new ApiException("任务的设备编号不能为空");
        }
        
        if (StrUtil.isBlank(request.getUserId())) {
            throw new ApiException("任务的用户ID不能为空");
        }
        
        // 发送请求
        CreateTaskResponse response = apiClient.doPost(PRE_CREATE_TASK_ENDPOINT, request, CreateTaskResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == 0) {
                log.info("任务预创建成功，任务ID: {}", 
                        (response.getData() != null ? response.getData().getTaskId() : "未知"));
            } else {
                log.error("任务预创建失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("任务预创建响应为空");
        }
        
        return response;
    }
}