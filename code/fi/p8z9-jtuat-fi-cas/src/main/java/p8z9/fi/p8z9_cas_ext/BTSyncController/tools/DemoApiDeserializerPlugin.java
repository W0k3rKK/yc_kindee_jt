package p8z9.fi.p8z9_cas_ext.BTSyncController.tools;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.nacos.common.http.param.MediaType;
import kd.bos.openapi.api.plugin.ApiDeserializerPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class DemoApiDeserializerPlugin  implements ApiDeserializerPlugin{


    private static final long serialVersionUID = 4568461229506221705L;

    @Override
    public Map<String, Object> deserializer(String request, String contentType) {
        try {
            if(contentType.contains("application/json")){
               //干扰入参
                List< Map<String, Object>> list= JSON.parseObject(request, new TypeReference<List<Map<String, Object>>>() {});
                Map<String, Object> map =new HashMap<>();
                map.put("payResultList",list);


                return map;

            }else if(contentType.contains(MediaType.APPLICATION_XML)){
                //XML格式反序列化逻辑
                return null;
            }else {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
