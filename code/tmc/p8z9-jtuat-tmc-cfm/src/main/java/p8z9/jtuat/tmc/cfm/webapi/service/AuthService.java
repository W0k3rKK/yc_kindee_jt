package p8z9.jtuat.tmc.cfm.webapi.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.auth.ChangePasswordRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.auth.LoginRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.auth.LoginResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

/**
 * 认证相关API服务
 */
public class AuthService {

    private static final Log log = LogFactory.get(AuthService.class);
    
    private final ApiClient apiClient;
    private final String LOGIN_ENDPOINT = "/sealFix/login"; // 对应 2.1.1
    private final String CHANGE_PASSWORD_ENDPOINT = "/sealFix/changePassword"; // 对应 2.1.2

    /**
     * 构造函数
     * @param apiClient API客户端实例
     */
    public AuthService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 调用"登录获取身份令牌"API
     * 根据印控系统对接指引，登录是所有操作的前提，必须先执行
     * 
     * @param request 登录请求
     * @return 登录响应，包含身份令牌
     * @throws ApiException API调用异常
     */
    public LoginResponse login(LoginRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("登录请求参数不能为空");
        }
        
        if (StrUtil.isBlank(request.getUserId())) {
            throw new ApiException("用户ID不能为空");
        }
        
        if (StrUtil.isBlank(request.getPassWord())) {
            throw new ApiException("密码不能为空");
        }
        
        log.info("正在调用登录接口，用户ID: {}", request.getUserId());
        
        // 发送请求
        LoginResponse response = apiClient.doPost(LOGIN_ENDPOINT, request, LoginResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == 0 && response.getData() != null) {
                // 如果登录成功，自动设置API客户端的认证令牌
                apiClient.setAuthToken(response.getData());
                log.info("登录成功，已设置认证令牌");
            } else {
                log.error("登录失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("登录响应为空");
        }
        
        return response;
    }

    /**
     * 调用"更换身份密文"API
     * 
     * @param request 修改密码请求
     * @return 基础响应
     * @throws ApiException API调用异常
     */
    public BaseResponse changePassword(ChangePasswordRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("修改密码请求参数不能为空");
        }
        
        if (StrUtil.isBlank(request.getUserId())) {
            throw new ApiException("用户ID不能为空");
        }
        
        if (StrUtil.isBlank(request.getOldPassWord())) {
            throw new ApiException("原密码不能为空");
        }
        
        if (StrUtil.isBlank(request.getNewPassWord())) {
            throw new ApiException("新密码不能为空");
        }
        
        if (StrUtil.isBlank(request.getConfirmPassWord())) {
            throw new ApiException("确认密码不能为空");
        }
        
        if (!request.getNewPassWord().equals(request.getConfirmPassWord())) {
            throw new ApiException("新密码与确认密码不一致");
        }
        
        log.info("正在调用修改密码接口，用户ID: {}", request.getUserId());
        
        // 发送请求
        BaseResponse response = apiClient.doPost(CHANGE_PASSWORD_ENDPOINT, request, BaseResponse.class);
        
        // 处理响应
        if (response != null) {
            if (response.getCode() == 0) {
                log.info("修改密码成功");
            } else {
                log.error("修改密码失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("修改密码响应为空");
        }
        
        return response;
    }
} 