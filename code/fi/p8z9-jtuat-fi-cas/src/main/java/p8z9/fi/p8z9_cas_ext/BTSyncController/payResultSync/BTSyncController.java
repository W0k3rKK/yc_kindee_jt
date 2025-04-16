package p8z9.fi.p8z9_cas_ext.BTSyncController.payResultSync;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.drew.lang.annotations.NotNull;
import kd.bos.context.RequestContext;
import kd.bos.context.RequestContextCreator;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.entity.operate.OperateOptionConst;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.custom.annotation.ApiController;
import kd.bos.openapi.common.custom.annotation.ApiParam;
import kd.bos.openapi.common.custom.annotation.ApiPostMapping;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.QueryServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import p8z9.fi.p8z9_cas_ext.BTSyncController.tools.PayResultEntity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@ApiController(value = "btsync", desc = "拜特集成")
public class BTSyncController   {

    private static final String PAYMENT_ENTITY = "cas_paybill";

    private static final Log logger = LogFactory.getLog(BTSyncController.class);




    /**
     * 付款结果明细
     * @param
     * @return
     * @throws ParseException
     */
    @ApiPostMapping(value = "/payresult", desc = "付款结果")
    public CustomApiResult<JSONArray> payResultSync(@NotNull   @ApiParam(value = "payResultList", required = true, message = "入参【payResultList】为空") List<PayResultEntity> payResultList) throws ParseException {

        logger.info("=============开始执行付款结果====================="+payResultList.toString());
        System.out.println("******************************");

        RequestContext requestContext = RequestContext.get();
        String accountId = requestContext.getAccountId();
        String tenantId = requestContext.getTenantId();
        String user = System.getProperty("user");
        RequestContextCreator.createForTripSI(tenantId,accountId,user);
        JSONArray result = new JSONArray();//返回结果状态
        if(payResultList.isEmpty()){
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("serial_no_erp","");
            jsonObject.put("is_success", "0");
            jsonObject.put("msg","入参为空");
            result.add(jsonObject);


            return CustomApiResult.success(result);
        }

        for (PayResultEntity info:payResultList) {

            String serial_id = info.getSerial_no_erp();//流水号
            String billno =info.getErp_bill_no();
            QFilter qFilter = new QFilter("billno", QCP.equals, billno);
            boolean exists = QueryServiceHelper.exists(PAYMENT_ENTITY, qFilter.toArray());
            if (!exists) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("serial_no_erp",serial_id);
                jsonObject.put("is_success", "0");
                jsonObject.put("msg","流水号不存在");
                result.add(jsonObject);
                continue;
            }

            List<DynamicObject> resultList = new ArrayList<>();
            String voucherStat = info.getVoucher_stat();
            DynamicObject bankDetail = BusinessDataServiceHelper.newDynamicObject(PAYMENT_ENTITY);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("serial_no_erp",serial_id);//结果流水号

            //付款状态、匹配标识：已付款改已匹配、未匹配付款金额改0、已付款金额把0修改为付款结果金额
            //0交易成功2交易失败-2打回
            DynamicObject dynamicObject=BusinessDataServiceHelper.loadSingle(PAYMENT_ENTITY,new QFilter("billno", QCP.equals, billno).toArray());
            if ("0".equals(voucherStat)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date parse = simpleDateFormat.parse(info.getTrans_time()==null?info.getOffline_date():info.getTrans_time());

                jsonObject.put("is_success", "1");
                jsonObject.put("msg","付款成功");
                //保存
                resultList.add(bankDetail);
                dynamicObject.set("modifytime", new Date());
                dynamicObject.set("paydate", parse);
                //自定义字段
                dynamicObject.set("p8z9_voucher_stat","0");
                OperateOption option = OperateOption.create();
                //传入自定义参数
                //设置该操作已经验权，无需再次验权
                option.setVariableValue(OperateOptionConst.ISHASRIGHT,"true");
                OperationResult  result1 = SaveServiceHelper.saveOperate(PAYMENT_ENTITY, new DynamicObject[]{dynamicObject}, OperateOption.create());

                System.out.println(result1);
            }else if ("2".equals(voucherStat)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                jsonObject.put("is_success", "1");
                jsonObject.put("msg","交易失败");
                resultList.add(bankDetail);

                dynamicObject.set("billstatus","G");
                dynamicObject.set("modifytime", new Date());
                OperateOption option = OperateOption.create();
                //传入自定义参数
                //设置该操作已经验权，无需再次验权
                option.setVariableValue(OperateOptionConst.ISHASRIGHT,"true");
                OperationResult  result1 = SaveServiceHelper.saveOperate(PAYMENT_ENTITY, new DynamicObject[]{dynamicObject}, OperateOption.create());

                System.out.println(result1);

            }else if ("-2".equals(voucherStat)) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                jsonObject.put("is_success", "1");
                jsonObject.put("msg",info.getReturnBack_msg());
                resultList.add(bankDetail);

                //自定义
                dynamicObject.set("p8z9_voucher_stat","-2");
                dynamicObject.set("p8z9_returnback_msg",info.getReturnBack_msg());
                dynamicObject.set("modifytime", new Date());
                OperateOption option = OperateOption.create();
                //传入自定义参数
                //设置该操作已经验权，无需再次验权
                option.setVariableValue(OperateOptionConst.ISHASRIGHT,"true");
                OperationResult  resultData = SaveServiceHelper.saveOperate(PAYMENT_ENTITY, new DynamicObject[]{dynamicObject}, OperateOption.create());

                System.out.println(resultData);
            }else{

                jsonObject.put("is_success", "0");
                jsonObject.put("msg","voucherStat不能为其它状态");

            }

            logger.info("=============付款结果voucherStat============="+voucherStat);
            result.add(jsonObject);


        }

        logger.info("=============付款结果成功============="+result);
        return CustomApiResult.success(result);
    }



}
