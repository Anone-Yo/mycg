package com.anone.wxPay.controller;

import com.alibaba.fastjson.JSON;
import com.anone.wxPay.service.WeiXinPayService;
import com.github.wxpay.sdk.WXPayUtil;
import com.sun.org.apache.regexp.internal.RE;
import entity.Result;
import entity.StatusCode;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/weixin/pay")
public class WxPayController {
    @Autowired
    private WeiXinPayService weiXinPayService;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建二维码
     * @param dataMap
     * @return
     */
    @RequestMapping(value = "/create/native")
    public Result createNative(@RequestParam Map<String,String> dataMap) {
        Map<String,String> resultMap = weiXinPayService.createNative(dataMap);
        return new Result(true, StatusCode.OK,"创建二维码成功",resultMap);
    }

    /**
     * 查询支付状态
     * @param outtradeno
     * @return
     */
    @GetMapping("/status/query")
    public Result queryStatus(String outtradeno) {
        Map<String,String> resultMap = weiXinPayService.queryPayStatus(outtradeno);
        return new Result(true,StatusCode.OK,"查询状态成功",resultMap);
    }

    /***
     * 支付结果通知的回调方法:作用是告知微信服务器，收到通知，不用再次发送信息过来
     */
    @RequestMapping(value = "/notify/url")
    public String notifyurl(HttpServletRequest request) throws Exception {
        //获取网路输入流
        ServletInputStream is = request.getInputStream();

        //获取字节数组输出流，来读取网路输入流
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        //定义缓存区，字节数组
        byte[] buffer=new byte[1024];
        //定义len变量
        int len=0;
        while ((len = is.read(buffer)) != -1) {
            baos.write(buffer,0,len);
        }
        //获取微信支付结果的字节数据--->转换为xml字符串
        byte[] bytes = baos.toByteArray();
        String xmlStr=new String(bytes,"utf-8");

        //将xml字符串转换成map
        Map<String, String> resultMap = WXPayUtil.xmlToMap(xmlStr);
        //将获取的返回信息，发送给mq中===》支付状态信息
        //获取map中的attach参数
        String attach = resultMap.get("attach");
        Map<String,String> attachMap = JSON.parseObject(attach, Map.class);
        rabbitTemplate.convertAndSend(attachMap.get("exchange"),attachMap.get("routingkey"), JSON.toJSONString(resultMap));

        //设置响应的数据==》返回给微信服务器
        //返回状态码 return_code  :success
        //返回信息   return_msg   :ok
        Map<String,String> messageMap=new HashMap<String, String>();
        messageMap.put("return_code","success");
        messageMap.put("return_msg","ok");
        return WXPayUtil.mapToXml(messageMap);

    }

    /**
     * 关闭订单
     */
    @GetMapping(value = "/close/order")
    public Result closeOrder(String outtradeno) {
        Map<String,String> resultMap = weiXinPayService.closeOrder(outtradeno);
        return new Result(true,StatusCode.OK,"关闭订单成功",resultMap);
    }

}
