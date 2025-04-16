package p8z9.tmc.bei.BTSyncController.transdetail;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.drew.lang.annotations.NotNull;
import kd.bos.context.RequestContext;
import kd.bos.context.RequestContextCreator;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.IOperateInfo;
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
import p8z9.tmc.bei.BTSyncController.tools.TransDetail;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@ApiController(value = "btsync", desc = "拜特集成")
public class BTSyncController {

    private static final String BANK_DETAIL_ENTITY = "bei_transdetail";
    private static final String PAYMENT_ENTITY = "cas_paybill";
    private static final Log logger = LogFactory.getLog(BTSyncController.class);





        /**
         * 交易流水明细
         * @param transDetailList
         * @return
         * @throws ParseException
         */
    @ApiPostMapping(value = "/transdetail", desc = "交易明细")
    public CustomApiResult<JSONArray> transDetailSync(@NotNull @Valid  @ApiParam(value = "transDetailList", required = true, message = "入参【transDetailList】为空") List<TransDetail> transDetailList) throws ParseException {
        logger.info("=================交易流水明细开始=============="+transDetailList.toString());
        RequestContext requestContext = RequestContext.get();
        String accountId = requestContext.getAccountId();
        String tenantId = requestContext.getTenantId();
        //mc配置读取
        String user = System.getProperty("user");


        RequestContextCreator.createForTripSI(tenantId,accountId,user);
        List<DynamicObject> bankDetailDataArray = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:mm");
        JSONArray result = new JSONArray();
        for (TransDetail info:transDetailList) {
            String serial_id = info.getSerial_id();
            QFilter qFilter = new QFilter("billno", QCP.equals, serial_id);
            boolean exists = QueryServiceHelper.exists(BANK_DETAIL_ENTITY, qFilter.toArray());
            if(exists){
                continue;
            }
            //select * from where xxx
            // qFilter.and("number", QCP.not_like, "10.%");
            DynamicObject bankDetail = BusinessDataServiceHelper.newDynamicObject(BANK_DETAIL_ENTITY);
            bankDetail.set("detailid", serial_id);
            bankDetail.set("bankdetailno", serial_id);

            bankDetail.set("billno", serial_id);
            bankDetail.set("uniqueseq", serial_id);
            String cdSign = info.getCd_sign();
            BigDecimal amt = new BigDecimal(info.getAmt() == null || "".equals(info.getAmt()) ? "0" : info.getAmt());
            bankDetail.set("isdowntobankstate", "0");
            bankDetail.set("debitamount", "0".equals(cdSign) ? BigDecimal.ZERO : amt);
            bankDetail.set("creditamount", "1".equals(cdSign) ? BigDecimal.ZERO : amt);
            bankDetail.set("oppbanknumber", info.getOpp_acc_no());
            bankDetail.set("oppunit", info.getOpp_acc_name());
            bankDetail.set("oppbank", info.getOpp_acc_bank());
            bankDetail.set("bizdate", dateFormat.parse(info.getTrans_time()));
            bankDetail.set("biztime", dateFormat.parse(info.getTrans_time()));
            bankDetail.set("p8z9_cd_sign",info.getCd_sign());//收支标识
            bankDetail.set("transdate", dateFormat.parse(info.getTrans_time()));
            bankDetail.set("description", info.getPostscript());
            String bankAcc = info.getBank_acc();
            bankDetail.set("billstatus", "A");
            bankDetail.set("receredtype", "0");
            bankDetail.set("datasource", "frombank");
            bankDetail.set("biztype", "1");
            bankDetail.set("sourcebillid", 0);
            bankDetail.set("currency", "1");
            bankDetail.set("ismatchereceipt", "c");
            bankDetail.set("bankcheckflag", "A");
            bankDetail.set("originalbankcheckflag", "1");
            bankDetail.set("bizrefno", "1");
            bankDetail.set("isdataimport", "1");
            bankDetail.set("isnoreceipt", "0");
            bankDetail.set("isrefund", "0");
            bankDetail.set("bankinterface", "1");
            bankDetail.set("isreced", "0");
            bankDetail.set("istransup", "c");
            bankDetail.set("istransdown", "c");
            bankDetail.set("isbankwithholding", "0");
            bankDetail.set("recedbilltype", "1");
            bankDetail.set("recedbillnumber", "1");
            bankDetail.set("receiptno", "S");
            bankDetail.set("sortno", 0);
            bankDetail.set("rulename", "1");
            bankDetail.set("isdownbankjournal", "c");
            bankDetail.set("errortype", "1");
            bankDetail.set("errormsg", "1");
            bankDetail.set("frmcod", "1");
            bankDetail.set("financialtype", 0);
            bankDetail.set("synonym", "c");
            //确认金额
            bankDetail.set("confirmamount", 1);
            //未确认金额
            bankDetail.set("unconfirmamount", 1);
            bankDetail.set("opprelacct", "1");
            bankDetail.set("refundsource", "1");
            bankDetail.set("iskdretflag","0");
            //查找银行账号
            DynamicObject bankAccount = BusinessDataServiceHelper.loadSingleFromCache("bd_accountbanks",
                    "id,openorg,bank,currency,defaultcurrency,acctname", new QFilter("bankaccountnumber", QCP.equals, bankAcc).toArray());
            if (bankAccount != null) {
                bankDetail.set("accountbank", bankAccount);
                bankDetail.set("company",  bankAccount.getDynamicObject("openorg"));
                bankDetail.set("bank", bankAccount.getDynamicObject("bank"));
                bankDetail.set("currency", getMulBaseDataCurrency(bankAccount));

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("serial_id",serial_id);
                jsonObject.put("is_success", "1");
                jsonObject.put("msg","成功");
                result.add(jsonObject);
            } else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("serial_id",serial_id);
                jsonObject.put("is_success", "0");
                jsonObject.put("msg",bankAcc +"不存在");
                result.add(jsonObject);
                continue;
            }
            bankDetail.set("creator", user);
            bankDetail.set("modifier", user);
            bankDetail.set("createtime", new Date());
            bankDetail.set("modifytime", new Date());
            bankDetailDataArray.add(bankDetail);

        }
        logger.info("=================交易流水明细bankDetailDataArray=============="+bankDetailDataArray);
        //保存到数据库
        OperationResult operationResult = SaveServiceHelper.saveOperate(BANK_DETAIL_ENTITY, bankDetailDataArray.toArray(new DynamicObject[0]), OperateOption.create());
        //检查数据是否有保存失败的数据
        List<IOperateInfo> allErrorOrValidateInfo = operationResult.getAllErrorOrValidateInfo();
        logger.info("=================交易流水明细保存完成operationResult================"+operationResult);
        if(!allErrorOrValidateInfo.isEmpty()){

            // 单据编号：xxxxxx
           // Object obj=allErrorOrValidateInfo.get(0).getPkValue();
              JSONObject jsonObject = new JSONObject();
              jsonObject.put("serial_id","");
              jsonObject.put("is_success", "0");
              jsonObject.put("msg", "保存失败");
              result.add(jsonObject);


        }

        logger.info("=================交易流水明细结束result================"+result);
        return CustomApiResult.success(result);
    }






    public  DynamicObject getMulBaseDataCurrency(DynamicObject bankAccount) {
        DynamicObject currency = null;
        DynamicObjectCollection currencyCol = bankAccount.getDynamicObjectCollection("currency");
        if (currencyCol != null && currencyCol.size() == 1) {
            DynamicObject dynamicObject = currencyCol.get(0);
            currency = dynamicObject.getDynamicObject("fbasedataid");
        } else {
            currency = bankAccount.getDynamicObject("defaultcurrency");
        }
        return currency;
    }

}
