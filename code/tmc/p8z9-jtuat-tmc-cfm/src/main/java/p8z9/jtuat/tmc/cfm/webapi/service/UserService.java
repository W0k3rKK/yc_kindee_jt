package p8z9.jtuat.tmc.cfm.webapi.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.ApiClient;
import p8z9.jtuat.tmc.cfm.webapi.dto.Code;
import p8z9.jtuat.tmc.cfm.webapi.dto.common.BaseResponse;
import p8z9.jtuat.tmc.cfm.webapi.dto.user.DeleteUserRequest;
import p8z9.jtuat.tmc.cfm.webapi.dto.user.UserSaveRequest;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

import java.util.List;

/**
 * 用户相关API服务
 */
public class UserService {

    private static final Log log = LogFactory.get(UserService.class);

    private final ApiClient apiClient;
    private final String SAVE_USERS_ENDPOINT = "/sealFix/saveUsers"; // 对应 2.2.1
    private final String DELETE_USERS_ENDPOINT = "/sealFix/deleteUsers"; // 对应 2.2.2

    /**
     * 构造函数
     *
     * @param apiClient API客户端实例
     */
    public UserService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    /**
     * 调用"同步用户"API
     * 根据印控系统对接指引，用户同步是必要的，确保用印人在印控系统中存在
     *
     * @param userList 用户列表
     * @return 基础响应
     * @throws ApiException API调用异常
     */
    public BaseResponse saveUsers(List<UserSaveRequest> userList) throws ApiException {
        // 参数校验
        if (CollUtil.isEmpty(userList)) {
            throw new ApiException("用户列表不能为空");
        }

        log.info("正在调用同步用户接口，用户数量: {}", userList.size());

        // 验证用户数据
        for (int i = 0; i < userList.size(); i++) {
            UserSaveRequest user = userList.get(i);

            if (StrUtil.isBlank(user.getUserId())) {
                throw new ApiException("第" + (i + 1) + "个用户的ID不能为空");
            }

            if (StrUtil.isBlank(user.getUserName())) {
                throw new ApiException("第" + (i + 1) + "个用户的名称不能为空");
            }

            if (StrUtil.isBlank(user.getPassWord())) {
                throw new ApiException("第" + (i + 1) + "个用户的密码不能为空");
            }

            if (StrUtil.isBlank(user.getOrgId())) {
                throw new ApiException("第" + (i + 1) + "个用户的组织ID不能为空");
            }

            log.info("用户 #{} - ID: {}, 名称: {}, 组织: {}",
                    i + 1, user.getUserId(), user.getUserName(), user.getOrgId());
        }

        // 发送请求
        BaseResponse response = apiClient.doPost(SAVE_USERS_ENDPOINT, userList, BaseResponse.class);

        // 处理响应
        if (response != null) {
            if (response.getCode() == Code.SUCCESS.getCode()) {
                log.info("同步用户成功，共 {} 个用户", userList.size());
            } else {
                log.error("同步用户失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("同步用户响应为空");
        }

        return response;
    }

    /**
     * 调用"删除用户"API
     *
     * @param request 删除用户请求
     * @return 基础响应
     * @throws ApiException API调用异常
     */
    public BaseResponse deleteUsers(DeleteUserRequest request) throws ApiException {
        // 参数校验
        if (request == null) {
            throw new ApiException("删除用户请求参数不能为空");
        }

        if (CollUtil.isEmpty(request.getUserIds())) {
            throw new ApiException("用户ID列表不能为空");
        }

        log.info("正在调用删除用户接口，用户ID数量: {}", request.getUserIds().size());

        // 发送请求
        BaseResponse response = apiClient.doPost(DELETE_USERS_ENDPOINT, request, BaseResponse.class);

        // 处理响应
        if (response != null) {
            if (response.getCode() == Code.SUCCESS.getCode()) {
                log.info("删除用户成功，共 {} 个用户", request.getUserIds().size());
            } else {
                log.error("删除用户失败: code={}, message={}", response.getCode(), response.getMessage());
            }
        } else {
            log.error("删除用户响应为空");
        }

        return response;
    }
} 