package p8z9.fi.p8z9_cas_ext.PaymentBillDataSync;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.shaded.com.google.gson.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import dm.jdbc.util.StringUtil;
import kd.bos.dataentity.OperateOption;
import kd.bos.dataentity.entity.DynamicObject;
import kd.bos.dataentity.entity.DynamicObjectCollection;
import kd.bos.entity.operate.result.OperationResult;
import kd.bos.entity.plugin.AbstractOperationServicePlugIn;
import kd.bos.entity.plugin.args.AfterOperationArgs;
import kd.bos.entity.plugin.args.EndOperationTransactionArgs;
import kd.bos.entity.plugin.args.ReturnOperationArgs;
import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import kd.bos.openapi.common.result.CustomApiResult;
import kd.bos.orm.query.QCP;
import kd.bos.orm.query.QFilter;
import kd.bos.servicehelper.BusinessDataServiceHelper;
import kd.bos.servicehelper.operation.SaveServiceHelper;
import org.apache.axis.client.Service;
import org.apache.axis.encoding.XMLType;
import p8z9.base.helper.ParametersUtils;
import p8z9.fi.p8z9_cas_ext.PaymentBillDataSync.tool.PaymentBillInfoEntity;

import javax.xml.namespace.QName;
import javax.xml.rpc.Call;
import javax.xml.rpc.ParameterMode;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * @Title: PaymentBillDataSync
 * @Package kd.xdwl.awrp.bt.datasync.impl
 * @Date
 * @description: 升级付款单数据同步至拜特
 */
public class PaymentBillDataSync extends AbstractOperationServicePlugIn {
    private static final String sys_code = "D0FCC8BA2D7F906B5B87F6398D6EBF70";
    private static final Log logger = LogFactory.getLog(PaymentBillDataSync.class);
    @Override
    public void endOperationTransaction(EndOperationTransactionArgs e) {
        // TODO 在此添加业务逻辑



    }

    @Override
    public void afterExecuteOperationTransaction(AfterOperationArgs e) {
        super.afterExecuteOperationTransaction(e);
    }

    @Override
    public void onReturnOperation(ReturnOperationArgs e) {
        super.onReturnOperation(e);

        Map<String, Object> operateMeta = this.operateMeta;
        Object key = operateMeta.get("key");
        if ("save".equals(key) && e.getOperationResult().isSuccess()) {

            DynamicObject nullObj = BusinessDataServiceHelper.newDynamicObject("cas_paybill");
            DynamicObject[] paymentBillArray= BusinessDataServiceHelper.load( e.getOperationResult().getSuccessPkIds().toArray(), nullObj.getDynamicObjectType());
            CustomApiResult customApiResult=PaymentBillDataSyncBT(paymentBillArray);


        }
    }

    public static CustomApiResult<JSONArray> PaymentBillDataSyncBT(DynamicObject[] paymentBillArray){

        logger.info("PaymentBillDataSync PaymentBillDataSyncBT 付款单同步开始"+Arrays.toString(paymentBillArray));


        try {
        List<PaymentBillInfoEntity> paymentBillInfos = new ArrayList<>();
            JSONArray result = new JSONArray();//返回结果状态
            BigDecimal bigDecimal_1= BigDecimal.ZERO;//业务类型1本金+利息
            BigDecimal bigDecimal_2= BigDecimal.ZERO;//业务类型2本金+利息
            BigDecimal bigDecimal_amt= BigDecimal.ZERO;//业务类型1、2合计金额
            int count=0;

            if( paymentBillArray == null || paymentBillArray.length == 0){
                    Object[] object= new Object[2];
                    object[0]="data:入参为空";
                    object[1]="errMsg:请求错误";
                    result.add(object);

                return   CustomApiResult.success(result);
            }

        for(DynamicObject paymentBill : paymentBillArray){
            logger.info("PaymentBillDataSync PaymentBillDataSyncBT 付款单进入循环"+Arrays.toString(paymentBillArray));
            count=count+1;
            PaymentBillInfoEntity paymentBillInfoEntity = new PaymentBillInfoEntity();
            //系统代码
            paymentBillInfoEntity.setSys_code(sys_code);
            //流水号
            paymentBillInfoEntity.setSerial_no_erp(paymentBill.getString("id"));
            //凭证号
            paymentBillInfoEntity.setErp_bill_no(paymentBill.getString("billno"));
            //期望支付日期
            Date expectDate = paymentBill.getDate("expectdate");
            //业务类型日期
            Date ywDate=paymentBill.getDate("bizdate");
            paymentBillInfoEntity.setWish_payday(  expectDate != null ?  new SimpleDateFormat("yyyy-MM-dd").format(expectDate):new SimpleDateFormat("yyyy-MM-dd").format(ywDate));
            //付款方单位代码
            String orgNumber = paymentBill.getString("org.number");
            paymentBillInfoEntity.setPay_code(orgNumber);
            //付款方帐号
            paymentBillInfoEntity.setPayee_acc(paymentBill.getString("payeracctbank.bankaccountnumber"));
            //付款方开户行
            String bebank = paymentBill.getString("payerbank.bebank.number");
            if(bebank == null || "".equals(bebank)){
                bebank = paymentBill.getString("payerbank.name");
            }
            paymentBillInfoEntity.setPayee_accname(StringUtil.isEmpty(bebank) ? "" : bebank);

            //付款账号inneraccount
            String setPayee_acc=paymentBill.getString("payeracctbank.number");
            //付款账号名称
            String zh=paymentBill.getString("inneraccount");
            //币别  为空时默认01人民币，不为空时ERP系统传入01  currency.number payeecurrency
            String currencyNumber = paymentBill.getString("currency.name");
            paymentBillInfoEntity.setCur_code("人民币".equals(currencyNumber)?"01":currencyNumber);
            //收款方账号payeebanknumlb
            paymentBillInfoEntity.setPayee_acc(paymentBill.getString("payeebanknum"));
            //收款方户名
            paymentBillInfoEntity.setPayee_accname(paymentBill.getString("payeename"));
            //收款方开户行
            paymentBillInfoEntity.setPayee_bank(paymentBill.getString("payeebankname"));
            //收款方联行号
            paymentBillInfoEntity.setPayee_bank_code(paymentBill.getString("recbanknumber"));
            //收款方所在省
            paymentBillInfoEntity.setPayee_addr_province(paymentBill.getString("recprovince"));
            //收款方所在市
            paymentBillInfoEntity.setPayee_addr_city(paymentBill.getString("reccity"));

            paymentBillInfoEntity.setAgency_corp(paymentBill.getString("p8z9_orgfield.number"));

            //交易类型  默认34
            paymentBillInfoEntity.setPay_type("34");
            //获取分录信息
            DynamicObjectCollection dynamicObjectCollection = paymentBill.getDynamicObjectCollection("entry");
            //摘要拼接
            String purpose="";
            //DynamicObject entry :dynamicObjectCollection
            for (int i=0;i<dynamicObjectCollection.size();i++) {
                DynamicObject entry=dynamicObjectCollection.get(i);
                if (dynamicObjectCollection.size() <= 2){
                    String remark = entry.getString("e_remark");
                    paymentBillInfoEntity.setBus_type2("");
                    paymentBillInfoEntity.setBus_money2("");
                    if(i==0){
                        if( remark.contains("利息")) {
                            //业务类型1
                            paymentBillInfoEntity.setBus_type1("03");
                            purpose=purpose+"利息;";
                        }
                            if( remark.contains("本金")) {
                            //业务类型1 归还贷款本金
                            //paymentBillInfoEntity.setBus_type1("归还贷款利息");
                            paymentBillInfoEntity.setBus_type1("02");
                            purpose=purpose+"归还贷款本金;";
                        }
                        bigDecimal_1 = bigDecimal_1.add(entry.getBigDecimal("e_payableamt"));
                        paymentBillInfoEntity.setBus_money1(bigDecimal_1.toString());
                        bigDecimal_amt=bigDecimal_amt.add(bigDecimal_1);

                    }
                    if(i==1){
                        //业务类型2
                        if(remark.contains("利息")) {
                            //业务类型1
                            paymentBillInfoEntity.setBus_type2("03");
                            purpose=purpose+"利息;";
                        }
                        if(remark.contains("本金")) {
                            //业务类型1
                            paymentBillInfoEntity.setBus_type2("02");
                            purpose=purpose+"归还贷款本金;";
                        }

                        //e_payableamt付款金额
                        bigDecimal_2 = bigDecimal_2.add(entry.getBigDecimal("e_payableamt"));
                        paymentBillInfoEntity.setBus_money2(bigDecimal_2.toString());
                        bigDecimal_amt=bigDecimal_amt.add(bigDecimal_2);

                    }
                }

            }


            //合计金额
            paymentBillInfoEntity.setAmt(bigDecimal_amt);
            if(bigDecimal_amt.equals(0)){
                paymentBillInfoEntity.setAmt(paymentBill.getBigDecimal("actpayamt"));
            }
            if(paymentBillInfoEntity.getBus_type1()==null){
                paymentBillInfoEntity.setBus_type1("");
                paymentBillInfoEntity.setBus_money1("");
            }
            if(paymentBillInfoEntity.getBus_type2()==null){
                paymentBillInfoEntity.setBus_type2("");
                paymentBillInfoEntity.setBus_money2("");

            }
            //摘要
            paymentBillInfoEntity.setPurpose(purpose);
            if("".equals(purpose)){
                paymentBillInfoEntity.setPurpose(paymentBill.getString("description"));
            }

            //附言
            paymentBillInfoEntity.setAdd_word("");
            //备注（存出保证金信息改放这个字段）
            paymentBillInfoEntity.setRemark("");
            //支付单位代码 1001
            //paymentBillInfoEntity.setPay_code(paymentBill.getString("org"));

            //流程名称id(对方系统可填入.当在2.2接收付款结果时,可在自己系统内进行关联处理)
            paymentBillInfoEntity.setWork_flow_id("");
            //付款方户名"payeracctbank.acctname"
            paymentBillInfoEntity.setPayer_accname(paymentBill.getString("org.name"));
            //付款银行账号 bankaccountnumber fpayeracctbankid acctname
            paymentBillInfoEntity.setPayer_acc(paymentBill.getString("payeracctbank.number"));
            //付款银行账号名称payerbank fpayerbankid
            paymentBillInfoEntity.setPayer_bank(paymentBill.getString("payerbank.name"));
            paymentBillInfos.add(paymentBillInfoEntity);
            ObjectMapper objectMapper = new ObjectMapper();
            //ObjectMapper obj=new ObjectMapper();
            String jsonData = objectMapper.writeValueAsString(paymentBillInfoEntity);
            String urlApi="http://10.0.70.102:8099/btERP/services/gxjtCommonInterface?wsdl";
            StringBuilder param = new StringBuilder();
            param.append("<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:ser=\"http://server.commonInterface.gxjt.server.webservice.byttersoft.com\">");
            param.append("<soapenv:Header/>");
            param.append("<soapenv:Body>");
            param.append("<ser:createPayBill>");
            param.append("<ser:param>");
            param.append("<![CDATA[");
            param.append( jsonData );
            param.append("]]>");
            param.append( "</ser:param>");
            param.append("</ser:createPayBill>");
            param.append("</soapenv:Body>");
            param.append("</soapenv:Envelope>");
            logger.info("PaymentBillDataSync param 付款单参数"+param);
            Object obj = webService(jsonData);
            logger.info("PaymentBillDataSync obj 付款单发送完成"+obj);
            result.add(obj);

            if(obj!=null) {

                JSONObject jsonObject = JSON.parseObject(obj.toString());
                Object newObj = jsonObject.get("data");
                JSONObject newjsonObj = JSON.parseObject(newObj.toString());
                String returnCode = newjsonObj.get("returnCode").toString();
                if("1".equals(returnCode)){

                    String billno=paymentBill.getString("billno");
                    if(!"".equals(billno)) {
                        DynamicObject dynamicObject = BusinessDataServiceHelper.loadSingle("cas_paybill", new QFilter("billno", QCP.equals, billno).toArray());

                        String billstatus= dynamicObject==null?"":dynamicObject.get("billstatus").toString();
                        if("A".equals(billstatus)){
                            dynamicObject.set("billstatus","C");
                            dynamicObject.set("p8z9_voucher_stat","1");
                            OperationResult result1 = SaveServiceHelper.saveOperate("cas_paybill", new DynamicObject[]{dynamicObject}, OperateOption.create());

                        }
                    }

                }

            }


        }


            return  CustomApiResult.success(result);

        }catch (Exception e){

            logger.error("ApiResultSerializationPlugin serialize 主数据接口返回值序列化异常,异常原因:" + e.getMessage(), e);
        }
        JSONArray JSONArray=new JSONArray();
        Object[] object= new Object[2];
        object[0]="data:入参为空";
        object[1]="errMsg:请求错误";
        JSONArray.add(object);
        return  CustomApiResult.success(JSONArray);

    }



    public static Object webService(String xmlBody1) {


      /*  String xmlBody= "    \"sys_code\": \"4b108fec1f6fd022c3135c2f4b8bce3b\",\n" +
                "    \"serial_no_erp\": \"000000000000000000012\",\n" +
                "    \"pay_code\": \"1001\",\n" +
                "    \"erp_bill_no\": \"CF0002\",\n" +
                "    \"payer_acc\": \"付款账号123\",\n" +
                "    \"payer_accname\": \"付款测试账号\",\n" +
                "    \"payer_bank\": \"中国银行\",\n" +
                "    \"payee_acc\": \"收款账号456\",\n" +
                "    \"payee_accname\": \"收款测试账号\",\n" +
                "    \"payee_bank\": \"ABC001\",\n" +
                "    \"payee_bank_code\": \"991290000015\",\n" +
                "    \"payee_addr_province\": \"广西壮族自治区\",\n" +
                "    \"payee_addr_city\": \"南宁市\",\n" +
                "    \"amt\": \"100\",\n" +
                "    \"cur_code\": \"01\",\n" +
                "    \"purpose\": \"用途rrr\",\n" +
                "    \"wish_payday\": \"2020-03-18\",\n" +
                "    \"pay_type\": \"34\",\n" +
                "    \"bus_type1\": \"27\",\n" +
                "    \"bus_money1\": \"10\",\n" +
                "    \"bus_type2\": \"28\",\n" +
                "    \"bus_money2\": \"90\",\n" +
                "    \"add_word\": \"备注附言111\"\n" +
                "}\n";*/

        Map<String, String> cas = ParametersUtils.getAppCustomParams("cas");

        // String endpoint = "http://10.0.70.102:8099/btERP/services/gxjtCommonInterface?wsdl";
        // String nameSpace = "http://server.commonInterface.gxjt.server.webservice.byttersoft.com";
        // 参数化环境信息
        String endpoint = cas.get("endpoint");
        String nameSpace = cas.get("nameSpace");

        String createPayBill = "createPayBill";
        Object Object=new Object();


        try {
            Service service = new Service();
            Call call =  service.createCall();
            // call.setProperty(org.apache.axis.client.Call.USERNAME_PROPERTY, "account"); // 账号
            // call.setProperty(org.apache.axis.client.Call.PASSWORD_PROPERTY, "password");// 密码
            call.setTargetEndpointAddress(endpoint);// 远程调用路径
            QName opAddEntry = new QName(nameSpace , "createPayBill" );
            // 设置参数名: 参数名,参数类型,参数模式
            call.addParameter("param", XMLType.XSD_STRING, ParameterMode.IN);
            call.setReturnType(XMLType.XSD_STRING);// 设置被调用方法的返回值类型
            Object obj= call.invoke(opAddEntry,new Object[]{xmlBody1});// 远程调用
            Object=obj;
            System.out.println("returnData" + obj.toString());


        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(e);

        }
        return Object;
    }


    /**
     * 解析返回报文，返回对象
     * */

    public static JSONArray returnXmlToBean(String xmlStr)  {
        Object obj = null;

        String []str=xmlStr.split("<ns:return>");
        Object []newStr=new Object[str.length-1];
        JSONArray newJSONArray=new JSONArray();

        for(int i=1;i< str.length;i++){
            String next=str[i];
            newStr[i-1]=next.split("</ns:return>")[0];

        }
        JsonObject JsonObject=new JsonObject();
        String end=newStr.toString();
        System.out.println(newStr.toString());
        for(int j=0;j<newStr.length;j++) {
            Object o = newStr[j];
            if (o != null) {
                String myStr=o.toString();
                myStr=myStr.replace("{","");
                myStr=myStr.replace("}","");
                myStr =myStr.replace("data","");
                //String s=o.getClass().getCanonicalName();
                myStr=myStr.substring(1);
                String[] endstr=myStr.split(",");

                for(int k=0;k< endstr.length;k++){

                    if(endstr[k].trim().contains("returnMsg")){
                        //JsonObject.add("returnMsg",(String)endstr[k].substring(endstr[k]));
                        JsonObject.addProperty("returnMsg",(String)endstr[k].substring(endstr[k].indexOf(":")));
                        //endstr[k].substring(endstr[k].indexOf(":"));
                        System.out.println();

                    }
                    if(endstr[k].trim().contains("returnCode")){
                        JsonObject.addProperty("returnCode",(String)endstr[k].substring(endstr[k].lastIndexOf(":")));
                        System.out.println();

                    }
                    if(endstr[k].trim().contains("errMsg")){

                        JsonObject.addProperty("errMsg",(String)endstr[k].substring(endstr[k].indexOf(":")));
                        System.out.println();

                    }


                    System.out.println();

                }

                newJSONArray.add(JsonObject);
            }
        }

        return newJSONArray;
    }


}
