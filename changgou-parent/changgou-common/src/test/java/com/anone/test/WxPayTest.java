package com.anone.test;

import com.github.wxpay.sdk.WXPayUtil;
import entity.HttpClient;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信SDK 测试
 */
public class WxPayTest {

    @Test
    public void testWxPay() throws Exception {
        //获取一个随机的字符串
        String nonceStr = WXPayUtil.generateNonceStr();
        System.out.println("随机字符串："+nonceStr);

        //将map转换成xml字符串
        Map<String,String> map=new HashMap<String, String>();
        map.put("name","kobe");
        map.put("num","24");
        map.put("team","LA");
        String xmlStr = WXPayUtil.mapToXml(map);
        System.out.println("map转换的xml字符串：\n"+xmlStr);

        //将map转换成xml字符串带有签名
        String signedXml = WXPayUtil.generateSignedXml(map, "partnerkey");
        System.out.println("将map转换成xml字符串带有签名: \n"+signedXml);

        //将xml字符串转换成map
        Map<String, String> resultMap = WXPayUtil.xmlToMap(signedXml);
        System.out.println("xml转换成map：\n"+resultMap);

        //将xml字符串发送出去
        String url="https://www.baidu.com";
        HttpClient httpClient=new HttpClient(url);
        //将xml字符串存入http客户端对象中
        httpClient.setXmlParam(signedXml);
        //判断是不是https请求
        httpClient.setHttps(true);
        //传递xml字符串，一定是使用post请求
        httpClient.post();
        String result = httpClient.getContent();
        System.out.println("获取结果："+result);
    }
}
