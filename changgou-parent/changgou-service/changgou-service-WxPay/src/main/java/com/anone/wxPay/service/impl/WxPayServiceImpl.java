package com.anone.wxPay.service.impl;

import com.anone.wxPay.service.WxPayService;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WxPayServiceImpl implements WxPayService {
    //应用ID
    @Value("${weixin.appid}")
    private String appid;
    //商户号
    @Value("${weixin.partner}")
    private String partner;
    //秘钥
    @Value("${weixin.partnerkey}")
    private String partnerkey;
    //支付回调地址
    @Value("${weixin.notifyurl}")
    private String notifyurl;


    /****
     * 创建二维码操作
     * @param parameterMap
     * @return
     */
    @Override
    public Map createnative(Map<String, String> parameterMap) {

        try {
            //参数
            Map<String,String> paramMap = new HashMap<String,String>();
            paramMap.put("appid",appid);
            paramMap.put("mch_id",partner);
            //随机字符串
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            paramMap.put("body","畅购商城商品不错");
            //订单号
            paramMap.put("out_trade_no",parameterMap.get("outtradeno"));
            //交易金额，单位：分
            paramMap.put("total_fee",parameterMap.get("totalfee"));
            paramMap.put("spbill_create_ip","127.0.0.1");
            //交易结果回调通知地址
            paramMap.put("notify_url",notifyurl);
            paramMap.put("trade_type","NATIVE");

            String signature = WXPayUtil.generateSignature(paramMap, partnerkey);
            System.out.println(signature);
            //Map转成XML字符串，可以携带签名
            String xmlparameters = WXPayUtil.generateSignedXml(paramMap,partnerkey);

            //URL地址
            String url ="https://api.mch.weixin.qq.com/pay/unifiedorder";
            HttpClient httpClient = new HttpClient(url);
            //提交方式
            httpClient.setHttps(true);
            //提交参数
            httpClient.setXmlParam(xmlparameters);
            //执行请求
            httpClient.post();

            //获取返回的数据
            String result = httpClient.getContent();

            //返回数据转成Map
            Map<String,String> resultMap = WXPayUtil.xmlToMap(result);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
