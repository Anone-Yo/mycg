package com.anone.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.anone.order.service.OrderService;
import com.github.wxpay.sdk.WXPayUtil;
import com.netflix.discovery.converters.Auto;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 用于监听支付的结果
 */
@Component
//设置监听的队列消息
@RabbitListener(queues = "${mq.pay.queue.order}")
public class orderMessageListener {
    @Autowired
    private OrderService orderService;

    /**
     * 支付结果监听
     * @param message
     */
    @RabbitHandler
    public void getMessage(String message) throws Exception {
        //获取字符结果
        Map<String,String> map = JSON.parseObject(message, Map.class);
        //需要对map中的返回信息进行校验
        //return_code   通信标识
        String return_code = map.get("return_code");
        if (return_code.equals("SUCCESS")) {
            //result_code  业务结果
            String result_code = map.get("result_code");
            //获取订单号
            String out_trade_no = map.get("out_trade_no");
            //支付成功
            if (result_code.equals("SUCCESS")) {
                //修改订单状态
                //修改支付时间
                //修改订单的支付状态
                //修改微信的交易流水号
                orderService.updateStatus(out_trade_no,map.get("time_out"),map.get("transaction_id"));
            }
        }
    }
}
