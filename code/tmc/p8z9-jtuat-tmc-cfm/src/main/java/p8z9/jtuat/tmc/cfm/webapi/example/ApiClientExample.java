package p8z9.jtuat.tmc.cfm.webapi.example;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.auth.ChangePasswordRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.auth.LoginRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.auth.LoginResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.device.ListDevicesRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.device.ListDevicesResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.seal.ListSealsRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.seal.ListSealsResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.seal.SealEnableRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.GetSealDragDataResultResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.GetSealDragEffectUrlResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.GetSealDragResultRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.InitiateSealDragRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.InitiateSealDragResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.SealDragFileInfo;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.SealDragPositionInfo;
import p8z9.jtuat.tmc.cfm.webapi.dto.sealdrag.SealDragResultDataItem;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.CreateTaskByFileIdRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.CreateTaskResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.PreCreateTaskRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.task.SealInfoDetails;
import p8z9.jtuat.tmc.cfm.webapi.dto.user.DeleteUserRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.user.UserSaveRequest;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;
import p8z9.jtuat.tmc.cfm.webapi.service.AuthService;
import p8z9.jtuat.tmc.cfm.webapi.service.DeviceService;
import p8z9.jtuat.tmc.cfm.webapi.service.SealDragService;
import p8z9.jtuat.tmc.cfm.webapi.service.SealService;
import p8z9.jtuat.tmc.cfm.webapi.service.TaskService;
import p8z9.jtuat.tmc.cfm.webapi.service.UserService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * API客户端使用示例
 * 根据印控系统对接指引优化的接口调用流程
 */
public class ApiClientExample {

    private static final Log log = LogFactory.get(ApiClientExample.class);
    private static final String API_BASE_URL = "http://10.0.70.40"; // 替换为实际的API地址

    public static void main(String[] args) {
        log.info("开始执行API调用示例");
        
        // 创建API客户端
        ApiClient apiClient = new ApiClient(API_BASE_URL);
        
        try {
            // 第一步：登录获取令牌（必须先执行）
            LoginResponse loginResponse = loginExample(apiClient);
            if (loginResponse == null || loginResponse.getCode() != 0) {
                log.error("登录失败，无法继续后续操作");
                return;
            }
            
            // 第二步：获取设备列表
            listDevicesExample(apiClient);
            
            // 第三步：获取印章列表
            listSealsExample(apiClient);
            
            // 第四步：发起印章拖拽
            // initiateSealDragExample(apiClient);
            
            // 第五步：获取印章拖拽结果
            // getSealDragResultExample(apiClient);
            
            // 第六步：任务预创建
            // preCreateTaskExample(apiClient);
            
            // 第七步：创建用印任务（实际业务操作）
            // createTaskExample(apiClient);
            
            // 以下是其他可选操作，根据实际业务需求执行
            // changePasswordExample(apiClient);
            // syncUsersExample(apiClient);
            // deleteUsersExample(apiClient);
            // enableSealExample(apiClient);
            
            log.info("API调用示例执行完成");
        } catch (ApiException e) {
            log.error("API调用异常: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 登录示例 - 必须先执行此步骤获取令牌
     * 对应印控系统对接指引步骤1：获取身份令牌 (2.1.1 获取调取接口的权限)
     */
    private static LoginResponse loginExample(ApiClient apiClient) throws ApiException {
        log.info("=== 步骤1：登录获取身份令牌 (2.1.1) ===");
        AuthService authService = new AuthService(apiClient);
        
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUserId("fixcom"); // 管理员账号
        
        // 使用Hutool的DigestUtil进行MD5加密，替换硬编码的MD5值
        String password = "4297F44B13955235245B2497399D7A93";
        // String md5Password = DigestUtil.md5Hex(password);
        String md5Password = password;
        loginRequest.setPassWord(md5Password);
        
        log.info("尝试登录系统，用户ID: {}", loginRequest.getUserId());
        LoginResponse response = authService.login(loginRequest);
        
        log.info("登录结果: code={}, message={}", response.getCode(), response.getMessage());
        if (response.getCode() == 0) {
            log.info("登录成功，获取到的令牌: {}", response.getData());
            // 设置API客户端的认证令牌，所有后续请求都会自动携带x-auth-token头
            apiClient.setAuthToken(response.getData());
        }
        
        return response;
    }
    
    /**
     * 获取设备列表示例
     * 对应印控系统对接指引步骤2：设备列表 (2.2.1 获取设备列表)
     */
    private static void listDevicesExample(ApiClient apiClient) throws ApiException {
        log.info("=== 步骤2：获取设备列表 (2.2.1) ===");
        DeviceService deviceService = new DeviceService(apiClient);
        
        ListDevicesRequest request = new ListDevicesRequest();
        // 可选参数：按机构过滤
        // request.setOrgIds("00,01");
        // 可选参数：按状态过滤，1-启用，0-停用
        request.setIsValid(1);
        
        log.info("尝试获取设备列表");
        ListDevicesResponse response = deviceService.listDevices(request);
        
        log.info("获取设备列表结果: code={}, message={}", response.getCode(), response.getMessage());
        if (response.getCode() == 0 && response.getData() != null) {
            log.info("获取到 {} 个设备", response.getData().size());
            // 可以进一步处理设备列表数据
            response.getData().forEach(device -> {
                log.info("设备: ID={}, 名称={}, 状态={}", 
                        device.getDevId(), device.getDevName(), 
                        device.getIsValid() == 1 ? "启用" : "停用");
            });
        }
    }
    
    /**
     * 获取印章列表示例
     * 对应印控系统对接指引步骤3：印章列表 (2.2.12 获取对应设备下的印章列表)
     */
    private static void listSealsExample(ApiClient apiClient) throws ApiException {
        log.info("=== 步骤3：获取印章列表 (2.2.12) ===");
        DeviceService deviceService = new DeviceService(apiClient);
        
        ListSealsRequest request = new ListSealsRequest();
        // 可选参数：按设备ID过滤
        request.setDevId("D001"); // 使用前面获取的设备ID
        // 可选参数：按印章种类过滤
        // request.setSealKind("01");
        // 可选参数：按机构过滤
        // request.setOrgIds("00,01");
        
        log.info("尝试获取印章列表，设备ID: {}", request.getDevId());
        ListSealsResponse response = deviceService.listSeals(request);
        
        log.info("获取印章列表结果: code={}, message={}", response.getCode(), response.getMessage());
        if (response.getCode() == 0 && response.getData() != null) {
            log.info("获取到 {} 个印章", response.getData().size());
            // 可以进一步处理印章列表数据
            response.getData().forEach(seal -> {
                log.info("印章: ID={}, 名称={}, 种类={}", 
                        seal.getSealStoreId(), seal.getSealName(), seal.getSealKindName());
            });
        }
    }
    
    /**
     * 发起印章拖拽示例
     * 对应印控系统对接指引步骤4：印章拖拽 (2.4.2 确定盖章位置)
     */
    private static void initiateSealDragExample(ApiClient apiClient) throws ApiException {
        log.info("=== 步骤4：发起印章拖拽 (2.4.2) ===");
        SealDragService sealDragService = new SealDragService(apiClient);
        
        // 创建印章拖拽请求
        InitiateSealDragRequest request = new InitiateSealDragRequest();
        request.setTradeNo("BIZ20240501001"); // 业务流水号，与后续任务创建保持一致
        
        // 设置文件信息
        List<SealDragFileInfo> sealFiles = new ArrayList<>();
        SealDragFileInfo fileInfo = new SealDragFileInfo();
        fileInfo.setExtFileId("http://example.com/files/contract.pdf"); // 文件下载地址
        fileInfo.setFileName("测试合同.pdf"); // 文件名称
        sealFiles.add(fileInfo);
        request.setSealFiles(sealFiles);
        
        // 设置任务类型和设备ID
        request.setTaskType("01"); // 01-打印用印
        request.setDevId("D001"); // 设备ID
        
        log.info("尝试发起印章拖拽，业务流水号: {}", request.getTradeNo());
        InitiateSealDragResponse response = sealDragService.initiateSealDragByFileId(request);
        
        log.info("发起印章拖拽结果: code={}, message={}", response.getCode(), response.getMessage());
        if (response.getCode() == 0 && response.getData() != null) {
            log.info("拖拽页面URL: {}", response.getData());
            // 此URL可以嵌入iframe或在新窗口打开，供用户进行印章拖拽操作
        }
    }
    
    /**
     * 获取印章拖拽结果示例
     * 对应印控系统对接指引步骤5：印章拖拽结果 (2.4.3 预览盖章位置)
     */
    private static void getSealDragResultExample(ApiClient apiClient) throws ApiException {
        log.info("=== 步骤5：获取印章拖拽结果 (2.4.3) ===");
        SealDragService sealDragService = new SealDragService(apiClient);
        
        // 创建获取拖拽结果请求
        GetSealDragResultRequest request = new GetSealDragResultRequest();
        request.setTradeNo("BIZ20240501001"); // 业务流水号，与发起拖拽时相同
        
        // 获取拖拽数据
        request.setIsData(1); // 1-获取拖拽数据，0-获取效果URL
        log.info("尝试获取印章拖拽数据，业务流水号: {}", request.getTradeNo());
        GetSealDragDataResultResponse dataResponse = sealDragService.getSealDragResultData(request);
        
        log.info("获取印章拖拽数据结果: code={}, message={}", dataResponse.getCode(), dataResponse.getMessage());
        if (dataResponse.getCode() == 0 && dataResponse.getData() != null) {
            log.info("获取到拖拽数据，文件数量: {}", dataResponse.getData().size());
            // 可以进一步处理拖拽数据
            dataResponse.getData().forEach(fileData -> {
                log.info("文件: 名称={}, 印章数量={}", 
                        fileData.getFileName(), 
                        fileData.getSealList() != null ? fileData.getSealList().size() : 0);
                
                // 打印印章位置信息
                if (fileData.getSealList() != null) {
                    fileData.getSealList().forEach(sealPos -> {
                        log.info("  印章: ID={}, 页码={}, X={}, Y={}", 
                                sealPos.getSealId(), sealPos.getPageNum(), 
                                sealPos.getPosX(), sealPos.getPosY());
                    });
                }
            });
        }
        
        // 获取拖拽效果URL
        request.setIsData(0); // 0-获取效果URL
        log.info("尝试获取印章拖拽效果URL，业务流水号: {}", request.getTradeNo());
        GetSealDragEffectUrlResponse urlResponse = sealDragService.getSealDragResultEffectUrl(request);
        
        log.info("获取印章拖拽效果URL结果: code={}, message={}", urlResponse.getCode(), urlResponse.getMessage());
        if (urlResponse.getCode() == 0 && urlResponse.getData() != null) {
            log.info("拖拽效果URL: {}", urlResponse.getData());
            // 此URL可以嵌入iframe或在新窗口打开，供用户预览拖拽效果
        }
    }
    
    /**
     * 任务预创建示例
     * 对应印控系统对接指引步骤6：任务预创建 (2.3.2 任务预创建)
     */
    private static void preCreateTaskExample(ApiClient apiClient) throws ApiException {
        log.info("=== 步骤6：任务预创建 (2.3.2) ===");
        TaskService taskService = new TaskService(apiClient);
        
        // 创建任务预创建请求
        PreCreateTaskRequest request = new PreCreateTaskRequest();
        request.setTradeNo("BIZ20240501001"); // 业务流水号
        request.setTradeTitle("测试合同"); // 业务标题
        request.setExtFileId("http://example.com/files/contract.pdf"); // 文件下载地址
        request.setFileName("测试合同.pdf"); // 文件名称
        request.setTaskType("01"); // 01-打印用印
        request.setDevId("D001"); // 设备编号
        request.setCopies(1); // 份数
        request.setUserId("1001"); // 操作人员账号（用印人）- 必须是已同步的用户
        request.setCreateUser("admin"); // 创建人（申请人）
        request.setIsConfirm(0); // 任务确认，0否1是
        
        log.info("尝试任务预创建，业务流水号: {}", request.getTradeNo());
        CreateTaskResponse response = taskService.preCreateTask(request);
        
        log.info("任务预创建结果: code={}, message={}", response.getCode(), response.getMessage());
        if (response.getCode() == 0 && response.getData() != null) {
            log.info("任务ID: {}", response.getData().getTaskId());
            log.info("子任务数量: {}", (response.getData().getTaskList() != null ? response.getData().getTaskList().size() : 0));
        }
    }
    
    /**
     * 修改密码示例 - 可选操作
     */
    private static void changePasswordExample(ApiClient apiClient) throws ApiException {
        log.info("=== 修改密码示例（可选操作）===");
        AuthService authService = new AuthService(apiClient);
        
        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setUserId("test");
        
        // 使用Hutool的DigestUtil进行MD5加密
        String oldPassword = "123456";
        String newPassword = "654321";
        request.setOldPassWord(DigestUtil.md5Hex(oldPassword));
        request.setNewPassWord(DigestUtil.md5Hex(newPassword));
        request.setConfirmPassWord(DigestUtil.md5Hex(newPassword));
        
        log.info("尝试修改密码，用户ID: {}", request.getUserId());
        BaseResponse response = authService.changePassword(request);
        
        log.info("修改密码结果: code={}, message={}", response.getCode(), response.getMessage());
    }
    
    /**
     * 同步用户示例 - 系统初始化时执行一次
     * 根据印控系统对接指引，用户同步是必要的，确保用印人在印控系统中存在
     */
    private static void syncUsersExample(ApiClient apiClient) throws ApiException {
        log.info("=== 同步用户示例 ===");
        UserService userService = new UserService(apiClient);
        
        List<UserSaveRequest> userList = new ArrayList<>();
        
        // 用户1
        UserSaveRequest user1 = new UserSaveRequest();
        user1.setOrgId("00");
        user1.setUserName("测试人员1");
        user1.setPassWord(DigestUtil.md5Hex("123456"));
        user1.setUserId("1001");
        userList.add(user1);
        
        // 用户2
        UserSaveRequest user2 = new UserSaveRequest();
        user2.setOrgId("00");
        user2.setUserName("测试人员2");
        user2.setPassWord(DigestUtil.md5Hex("123456"));
        user2.setUserId("1002");
        userList.add(user2);
        
        log.info("尝试同步 {} 个用户", userList.size());
        BaseResponse response = userService.saveUsers(userList);
        
        log.info("同步用户结果: code={}, message={}", response.getCode(), response.getMessage());
    }
    
    /**
     * 删除用户示例 - 可选操作
     */
    private static void deleteUsersExample(ApiClient apiClient) throws ApiException {
        log.info("=== 删除用户示例（可选操作）===");
        UserService userService = new UserService(apiClient);
        
        DeleteUserRequest request = new DeleteUserRequest();
        request.setUserIds(Arrays.asList("1001", "1002"));
        
        log.info("尝试删除用户: {}", StrUtil.join(", ", request.getUserIds()));
        BaseResponse response = userService.deleteUsers(request);
        
        log.info("删除用户结果: code={}, message={}", response.getCode(), response.getMessage());
    }
    
    /**
     * 启用/停用印章示例 - 可选操作
     */
    private static void enableSealExample(ApiClient apiClient) throws ApiException {
        log.info("=== 启用/停用印章示例（可选操作）===");
        SealService sealService = new SealService(apiClient);
        
        SealEnableRequest request = new SealEnableRequest();
        request.setSealId("S001");
        request.setSealStatus(1); // 1-启用，0-停用
        
        log.info("尝试{}印章: {}", request.getSealStatus() == 1 ? "启用" : "停用", request.getSealId());
        BaseResponse response = sealService.enableSeal(request);
        
        log.info("启用/停用印章结果: code={}, message={}", response.getCode(), response.getMessage());
    }
    
    /**
     * 创建用印任务示例 - 实际业务操作
     * 对应印控系统对接指引步骤7：创建任务 (2.3.1 创建任务)
     */
    private static void createTaskExample(ApiClient apiClient) throws ApiException {
        log.info("=== 步骤7：创建用印任务 (2.3.1) ===");
        TaskService taskService = new TaskService(apiClient);
        
        List<CreateTaskByFileIdRequest> requestList = new ArrayList<>();
        
        // 创建一个用印任务
        CreateTaskByFileIdRequest task = new CreateTaskByFileIdRequest();
        task.setTradeNo("BIZ20240501001"); // 业务流水号
        task.setTradeTitle("测试合同"); // 业务标题
        task.setExtFileId("http://example.com/files/contract.pdf"); // 文件下载地址
        task.setFileName("测试合同.pdf"); // 文件名称
        task.setTaskType("01"); // 01-打印用印
        task.setDevId("D001"); // 设备编号
        task.setCopies(1); // 份数
        task.setUserId("1001"); // 操作人员账号（用印人）- 必须是已同步的用户
        task.setCreateUser("admin"); // 创建人（申请人）
        task.setIsConfirm(0); // 任务确认，0否1是
        
        // 设置印章信息
        SealInfoDetails sealInfo = new SealInfoDetails();
        sealInfo.setSealType(1); // 1-普通印章
        sealInfo.setSealId("S001"); // 印章ID
        sealInfo.setSealName("公司公章"); // 印章名称
        sealInfo.setIsWaterMark(0); // 0-不使用水印
        sealInfo.setSealPosType(1); // 1-坐标定位
        sealInfo.setPageNum(1); // 第1页
        sealInfo.setPosX(300); // X坐标
        sealInfo.setPosY(500); // Y坐标
        sealInfo.setSealDirection(0); // 0-正向
        
        task.setSealInfo(sealInfo);
        requestList.add(task);
        
        log.info("尝试创建用印任务，业务流水号: {}", task.getTradeNo());
        CreateTaskResponse response = taskService.createTaskByFileId(requestList);
        
        log.info("创建用印任务结果: code={}, message={}", response.getCode(), response.getMessage());
        if (response.getCode() == 0 && response.getData() != null) {
            log.info("任务ID: {}", response.getData().getTaskId());
            log.info("子任务数量: {}", (response.getData().getTaskList() != null ? response.getData().getTaskList().size() : 0));
        }
    }
} 