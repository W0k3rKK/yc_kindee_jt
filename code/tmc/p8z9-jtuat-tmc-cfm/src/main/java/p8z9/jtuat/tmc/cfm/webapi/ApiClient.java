package p8z9.jtuat.tmc.cfm.webapi;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

/**
 * API客户端
 * 负责与印控系统API进行HTTP通信
 * 使用Hutool工具库实现HTTP请求和JSON处理
 */
public class ApiClient {

    private static final Log log = LogFactory.get(ApiClient.class);
    
    private final String baseUrl;
    private String authToken;
    
    // 超时设置（毫秒）
    private static final int CONNECT_TIMEOUT = 10000; // 10秒
    private static final int READ_TIMEOUT = 30000;    // 30秒
    
    // 重试设置
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 1000;      // 1秒

    /**
     * 构造函数
     * @param baseUrl API基础URL
     */
    public ApiClient(String baseUrl) {
        // 规范化基础URL，确保不以"/"结尾
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        log.info("初始化API客户端，基础URL: {}", this.baseUrl);
    }

    /**
     * 设置认证令牌
     * @param authToken 认证令牌
     */
    public void setAuthToken(String authToken) {
        this.authToken = authToken;
        log.debug("设置认证令牌: {}", authToken);
    }

    /**
     * 执行POST请求
     * @param endpoint API端点
     * @param requestBody 请求体对象
     * @param responseType 响应类型
     * @param <T> 响应类型泛型
     * @return 响应对象
     * @throws ApiException API调用异常
     */
    public <T> T doPost(String endpoint, Object requestBody, Class<T> responseType) throws ApiException {
        String url = baseUrl + endpoint;
        log.info("发送POST请求到: {}", url);
        
        try {
            // 将请求对象转换为JSON字符串
            String requestBodyJson = JSONUtil.toJsonStr(requestBody);
            log.debug("请求体: {}", requestBodyJson);
            
            // 执行请求，支持重试
            HttpResponse response = executeWithRetry(url, requestBodyJson);
            
            // 获取响应状态码和响应体
            int statusCode = response.getStatus();
            String responseBody = response.body();
            
            log.info("响应状态码: {}", statusCode);
            log.debug("响应体: {}", responseBody);
            
            // 处理响应
            if (statusCode >= HttpStatus.HTTP_OK && statusCode < HttpStatus.HTTP_MULT_CHOICE) {
                // 成功响应，将JSON转换为对象
                return JSONUtil.toBean(responseBody, responseType);
            } else {
                // 错误响应
                throw new ApiException("API请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
            }
        } catch (Exception e) {
            if (e instanceof ApiException) {
                throw (ApiException) e;
            }
            throw new ApiException("API请求异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行HTTP请求，支持重试
     * @param url 完整URL
     * @param requestBody 请求体JSON字符串
     * @return HTTP响应
     * @throws ApiException API调用异常
     * @throws InterruptedException 中断异常
     */
    private HttpResponse executeWithRetry(String url, String requestBody) 
            throws ApiException, InterruptedException {
        
        Exception lastException = null;
        
        // 重试循环
        for (int attempt = 0; attempt < MAX_RETRY_COUNT; attempt++) {
            try {
                if (attempt > 0) {
                    log.warn("重试请求，第 {} 次尝试", (attempt + 1));
                    Thread.sleep(RETRY_DELAY);
                }
                
                // 创建HTTP请求
                HttpRequest request = HttpRequest.post(url)
                        .timeout(READ_TIMEOUT) // 读取超时
                        .setConnectionTimeout(CONNECT_TIMEOUT) // 连接超时
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .body(requestBody);
                
                // 如果有认证令牌，添加到请求头
                if (StrUtil.isNotBlank(authToken)) {
                    request.header("x-auth-token", authToken);
                }
                
                // 执行请求
                HttpResponse response = request.execute();
                
                // 如果是服务器错误（5xx），进行重试
                int statusCode = response.getStatus();
                if (statusCode >= HttpStatus.HTTP_INTERNAL_ERROR) {
                    log.error("服务器错误，状态码: {}，准备重试", statusCode);
                    lastException = new ApiException("服务器错误，状态码: " + statusCode);
                    continue;
                }
                
                return response;
            } catch (Exception e) {
                log.error("请求异常: {}，准备重试", e.getMessage());
                lastException = e;
            }
        }
        
        // 所有重试都失败了
        throw new ApiException("请求失败，已重试 " + MAX_RETRY_COUNT + " 次: " + 
                              (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }
}