package com.anone.wxPay.service.impl;

import com.alibaba.fastjson.JSON;
import com.anone.wxPay.service.WeiXinPayService;

import com.github.wxpay.sdk.WXPayConstants;
import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeiXinPayServiceImpl implements WeiXinPayService {
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


;
    /***
     *  创建二维码
     * @param dataMap
     * @return
     */
    @Override
    public Map createNative(Map<String, String> dataMap) {
        try {
            Map<String,String> parameterMap=new HashMap<>();
            //参数
            //appid公众号账号id
            parameterMap.put("appid",appid);
            //商户号mch_id
            parameterMap.put("mch_id",partner);
            //随机字符串 nonce_str
            parameterMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //商户订单号 out_trade_no
            parameterMap.put("out_trade_no",dataMap.get("outtradeno"));
            //商品描述 body
            parameterMap.put("body","anone为你代言");
            //标价金额--付款金额----单位为分total_fee
            parameterMap.put("total_fee",dataMap.get("totalfee"));
            //终端ip  spbill_create_ip
            parameterMap.put("spbill_create_ip","127.0.0.1");
            //支付回调地址 notify_url
            parameterMap.put("notify_url",notifyurl);
            //交易类型 trade_type
            parameterMap.put("trade_type","NATIVE");
            //封装交换机和队列信息
            Map<String,String> attachMap=new HashMap<String, String>();
            attachMap.put("exchange",dataMap.get("exchange"));
            attachMap.put("queue",dataMap.get("routingkey"));
            attachMap.put("username",dataMap.get("username"));
            //将map转换成json字符串
            String attach = JSON.toJSONString(attachMap);
            parameterMap.put("attach",attach);

            //签名--->map转成xml字符串会带有签名
            /*String xmlparameters2 = WXPayUtil.mapToXml(parameterMap);
            System.out.println(xmlparameters2);*/
            String xmlparameters = WXPayUtil.generateSignedXml(parameterMap,partnerkey);
            System.out.println(xmlparameters);


            //地址
            String url="https://api.mch.weixin.qq.com/pay/unifiedorder";
            //提交方式
            HttpClient client=new HttpClient(url);
            //提交参数
            client.setXmlParam(xmlparameters);
            //执行请求
            client.post();
            //获取返回的数据
            String content = client.getContent();
            //将返回数据转换成map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);

            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 查询订单状态
     * @param outtradeno
     * @return
     */
    @Override
    public Map queryPayStatus(String outtradeno) {
        try {
            Map<String,String> parameterMap=new HashMap<String, String>();
            //参数
            //appid
            parameterMap.put("appid",appid);
            //mch_id
            parameterMap.put("mch_id",partner);
            //订单号
            parameterMap.put("out_trade_no",outtradeno);
            //随机字符串 nonce_Str
            parameterMap.put("nonce_Str",WXPayUtil.generateNonceStr());
            //签名
            String xmlStr =WXPayUtil.generateSignedXml(parameterMap, partnerkey);

            //url地址
            String url="https://api.mch.weixin.qq.com/pay/orderquery";
           /* HttpClient client=new HttpClient(url);
            //提交方式
            client.setXmlParam(xmlStr);
            //提交参数
            client.setHttps(true);
            //执行请求
            client.post();
            //获取返回的xml字符串数据
            String content = client.getContent();
            //将xml数据转成map
            Map<String, String> resultMap = WXPayUtil.xmlToMap(content);*/
            Map<String, String> resultMap = getResultMap(xmlStr, url);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 关闭订单
     * @param outtradeno
     * @return
     */
    @Override
    public Map closeOrder(String outtradeno) {
        try {
            Map<String,String> parameterMap=new HashMap<String, String>();
            //请求参数
            //appid
            parameterMap.put("appid",appid);
            //mch_id
            parameterMap.put("mch_id",partner);
            //订单号
            parameterMap.put("out_trade_no",outtradeno);
            //随机字符串
            parameterMap.put("nonce_str",WXPayUtil.generateNonceStr());
            //签名
            String xmlStr = WXPayUtil.generateSignedXml(parameterMap, partnerkey);

            //url地址
            String url="https://api.mch.weixin.qq.com/pay/closeorder";
            Map<String, String> resultMap = getResultMap(xmlStr, url);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 提取使用httpClient发起请求，获取返回结果
     * @param xmlStr  xml字符串 请求的数据
     * @param url      请求的路径  接口文档中定义好
     * @return
     * @throws Exception
     */
    public Map<String, String> getResultMap(String xmlStr, String url) throws Exception {
        HttpClient client=new HttpClient(url);
        //提交方式
        client.setHttps(true);
        //提交参数
        client.setXmlParam(xmlStr);
        //执行请求
        client.post();
        //获取返回xml字符串
        String content = client.getContent();
        //将xml字符串转为map
        return WXPayUtil.xmlToMap(content);
    }


}
