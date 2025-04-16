package p8z9.fi.p8z9_cas_ext.BTSyncController.tools;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.http.param.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import kd.bos.exception.ErrorCode;
import kd.bos.exception.KDBizException;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.api.plugin.ApiSerializerPlugin;
import kd.bos.openapi.api.plugin.SerializerResult;

/**
 * @Title: ApiResultdeserializationPlugin
 * @Author libai
 * @Package gxdm.masterdata
 * @Date 2024/12/20 17:24
 * @description: 返回结果反序列化
 */
public class ApiResultSerializationPlugin implements ApiSerializerPlugin {

    private static final long serialVersionUID = 8766226690626265782L;
    private final Log logger = LogFactory.getLog(ApiResultSerializationPlugin.class);

    @Override
    public SerializerResult serialize(Object response, String accept, String contentType) {
        String result = null;

        if(MediaType.APPLICATION_JSON.contains(contentType)){
            try {
                result = new ObjectMapper().writeValueAsString(response);
                JSONObject oldResult = JSONObject.parseObject(result);
                result=oldResult.get("data").toString();
                JSONArray jSONArray =JSONArray.parseArray(result);

                JSONArray newJSONArray=new JSONArray();
                for(Object o:jSONArray){
                    JSONObject newResult = new JSONObject();
                    JSONObject jSONObject = JSONObject.parseObject(o.toString());
                    //付款结果
                    if(jSONObject.get("serial_no_erp")!=null||"".equals(jSONObject.get("serial_no_erp"))) {
                        newResult.put("serial_no_erp", jSONObject.get("serial_no_erp"));
                    }
                    //付款流水
                    if(jSONObject.get("serial_id")!=null||"".equals(jSONObject.get("serial_id"))){
                        newResult.put("serial_id", jSONObject.get("serial_id"));
                    }
                    newResult.put("is_success", jSONObject.get("is_success"));
                    newResult.put("msg", jSONObject.getString("msg"));
                    newJSONArray.add(newResult);
                }

                result=newJSONArray.toString();

            } catch (Exception e) {
                logger.error("ApiResultSerializationPlugin serialize relust = " + result, e);
                logger.error("ApiResultSerializationPlugin serialize 主数据接口返回值序列化异常,异常原因:" + e.getMessage(), e);
                throw new KDBizException(new ErrorCode("990","主数据接口返回值序列化异常,异常原因:" + e.getMessage()),e);
            }
        }
        return new SerializerResult(MediaType.APPLICATION_JSON, result);
    }
}
