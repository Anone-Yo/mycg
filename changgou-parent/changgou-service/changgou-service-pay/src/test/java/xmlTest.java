import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;


public class xmlTest {
    //应用id
    @Value("${weixin.appid}")
    private String  appid;
    //商户id
    @Value("${weixin.partner}")
    private String  partner;
    //秘钥
    @Value("${weixin.partnerkey}")
    private String  partnerkey;
    //支付回调的地址
    @Value("${weixin.notifyurl}")
    private String  notifyurl;


    @Test
    public void testXmlStr() throws Exception {

        Map<String,String> parameterMap=new HashMap<>();
        parameterMap.put("appid","wx839f8696b538317");
        parameterMap.put("mch_id","1473426802");
        //随机字符串
        parameterMap.put("nonce_str", WXPayUtil.generateNonceStr());
        System.out.println(WXPayUtil.generateNonceStr());
        parameterMap.put("body","畅购商城商品不错");
        //订单号
        parameterMap.put("out_trade_no","1999999");
        //交易金额，单位：分
        parameterMap.put("total_fee","1");
        parameterMap.put("spbill_create_ip","127.0.0.1");
        //交易结果回调通知地址
        parameterMap.put("notify_url","http://2cw4969042.wicp.vip:38608/weixin/pay/notify/url");
        parameterMap.put("trade_type","NATIVE");

       //Map转成XML字符串，可以携带签名
       String xmlparameters = WXPayUtil.generateSignedXml(parameterMap,"T6m9iK73b0kn9g5v426MKfHQH7X8rKwb");
        System.out.println(xmlparameters);
    }
}
