package p8z9.base.helper;

import kd.bos.entity.AppInfo;
import kd.bos.entity.AppMetadataCache;
import kd.bos.entity.param.AppCustomParam;
import kd.bos.servicehelper.parameter.SystemParamServiceHelper;

import java.util.Map;

public class ParametersUtils {
    /**
     * 获取应用自定义参数
     *
     * @param appNum 应用标识
     * @return 应用自定义参数
     */
    public static Map<String, String> getAppCustomParams(String appNum) {
        //从缓存中获取应用信息
        AppInfo ptstDemo = AppMetadataCache.getAppInfo(appNum);
        //获取应用的主键
        String appId = ptstDemo.getId();
        AppCustomParam apm = new AppCustomParam();
        apm.setAppId(appId);
        //获取整体应用参数
        return SystemParamServiceHelper.loadAppCustomParameterFromCache(apm);
    }
}
