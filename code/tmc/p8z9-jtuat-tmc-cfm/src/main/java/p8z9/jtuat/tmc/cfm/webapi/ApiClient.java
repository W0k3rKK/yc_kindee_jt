package p8z9.jtuat.tmc.cfm.webapi;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpStatus;
import cn.hutool.json.JSONUtil;
import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import p8z9.jtuat.tmc.cfm.webapi.exception.ApiException;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * API客户端 (最终修复版)
 * 负责与印控系统API进行HTTP通信
 * - 提取了通用的请求执行和重试逻辑
 * - 修正了doPostMultipart方法，以正确处理List<File>类型的文件上传
 */
public class ApiClient {

    private static final Log log = LogFactory.get(ApiClient.class);

    private final String baseUrl;
    private String authToken;

    // 超时设置（毫秒）
    private static final int CONNECT_TIMEOUT = 10000; // 10秒
    private static final int READ_TIMEOUT = 60000;    // 60秒 (文件上传可能需要更长时间)

    // 重试设置
    private static final int MAX_RETRY_COUNT = 3;
    private static final int RETRY_DELAY = 1000;      // 1秒

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        log.info("初始化API客户端，基础URL: {}", this.baseUrl);
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
        log.info("设置认证令牌: {}", authToken);
    }

    public <T> T doPost(String endpoint, Object requestBody, Class<T> responseType) throws ApiException {
        String url = baseUrl + endpoint;
        String requestBodyJson = JSONUtil.toJsonStr(requestBody);

        log.info("发送POST (JSON)请求到: {}", url);
        log.info("请求体: {}", requestBodyJson);

        HttpRequest request = HttpRequest.post(url)
                .header("Content-Type", "application/json")
                .body(requestBodyJson);

        String responseBody = executeRequest(request);
        return JSONUtil.toBean(responseBody, responseType);
    }

    /**
     * 执行POST请求 (multipart/form-data) - 【已修正对 List<File> 的处理】
     *
     * @param endpoint     API端点
     * @param formData     表单数据，Key为字段名，Value可以是普通值或文件(File, File[], List<File>)
     * @param responseType 响应类型
     * @param <T>          响应类型泛型
     * @return 响应对象
     * @throws ApiException API调用异常
     */
    public <T> T doPostMultipart(String endpoint, Map<String, Object> formData, Class<T> responseType) throws ApiException {
        String url = baseUrl + endpoint;
        log.info("发送POST (Multipart)请求到: {}", url);
        if (formData != null) {
            log.info("表单数据键: {}", formData.keySet());
        }

        // 构建请求
        HttpRequest request = HttpRequest.post(url);

        // 手动遍历Map，修复Hutool对 List<File> 的处理缺陷
        if (formData != null && !formData.isEmpty()) {
            for (Map.Entry<String, Object> entry : formData.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();

                if (value == null) {
                    continue; // 忽略null值
                }

                // 检查是否为List<File>的特殊情况
                if (value instanceof List && !((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof File) {
                    // 正确处理 List<File>：遍历列表，为每个文件单独调用 form 方法
                    @SuppressWarnings("unchecked") // 在if中已做过类型判断，此处转换是安全的
                    List<File> files = (List<File>) value;
                    for (File file : files) {
                        // 这会正确地为每个文件创建一个 multipart part
                        request.form(key, file);
                    }
                } else {
                    // 对于其他所有类型(String, Integer, 单个File等)，Hutool的默认处理是正确的
                    request.form(key, value);
                }
            }
        }

        // 执行请求并解析响应
        String responseBody = executeRequest(request);
        return JSONUtil.toBean(responseBody, responseType);
    }

    private String executeRequest(HttpRequest request) throws ApiException {
        request.timeout(READ_TIMEOUT)
                .setConnectionTimeout(CONNECT_TIMEOUT)
                .header("Accept", "application/json");

        if (StrUtil.isNotBlank(authToken)) {
            request.header("x-auth-token", authToken);
        }

        HttpResponse response = executeWithRetry(request::execute);

        int statusCode = response.getStatus();
        String responseBody = response.body();

        log.info("响应状态码: {}", statusCode);
        log.info("响应体: {}", responseBody);

        if (statusCode >= HttpStatus.HTTP_OK && statusCode < HttpStatus.HTTP_MULT_CHOICE) {
            return responseBody;
        } else {
            throw new ApiException("API请求失败，状态码: " + statusCode + ", 响应: " + responseBody);
        }
    }

    private HttpResponse executeWithRetry(Supplier<HttpResponse> requestExecutor) throws ApiException {
        Exception lastException = null;

        for (int attempt = 0; attempt < MAX_RETRY_COUNT; attempt++) {
            try {
                if (attempt > 0) {
                    log.warn("准备重试请求，第 {} 次尝试...", (attempt + 1));
                    Thread.sleep(RETRY_DELAY * attempt);
                }

                HttpResponse response = requestExecutor.get();

                if (response.getStatus() >= HttpStatus.HTTP_INTERNAL_ERROR) {
                    log.error("服务器错误，状态码: {}，准备重试", response.getStatus());
                    lastException = new ApiException("服务器错误，状态码: " + response.getStatus());
                    continue;
                }

                return response;

            } catch (Exception e) {
                log.error("请求发生异常: {}，准备重试", e.getMessage());
                lastException = e;
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        throw new ApiException("请求失败，已达到最大重试次数 (" + MAX_RETRY_COUNT + " 次): " +
                (lastException != null ? lastException.getMessage() : "未知错误"), lastException);
    }
}