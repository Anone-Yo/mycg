package com.anone.seckill.mq.listener;

import com.alibaba.fastjson.JSON;
import com.anone.seckill.service.SeckillOrderService;
import com.anone.wxpay.feign.WxPayFeign;
import entity.IdWorker;
import entity.Result;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 秒杀订单状态监听
 */
@Component
@RabbitListener(queues = "${mq.pay.queue.seckillorder}")
public class SeckillMessageListener {

    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private WxPayFeign wxPayFeign;

    @RabbitHandler
    public void getMessage(String message) {
        //将json字符转成map
        Map<String,String> resultMap = JSON.parseObject(message, Map.class);
        //获取通信标识
        String return_code = resultMap.get("return_code");
        if (return_code.equals("SUCCESS")) {
            //获取业务结果
            String result_code = resultMap.get("result_code");
            //获取订单号
            String outTradeNo = resultMap.get("out_trade_no");
            String attach = resultMap.get("attach");
            Map<String,String> attachMap = JSON.parseObject(attach, Map.class);
            //获取用户名
            String username=resultMap.get("username");
            if (result_code.equals("SUCCESS")) {
                //生成流水号
                String transcationId = String.valueOf(idWorker.nextId());
                //获取支付时间
                String time_end=resultMap.get("time_end");
                //成功支付，修改订单状态
                seckillOrderService.updateSeckillOrderStatus(time_end, transcationId, username);
            } else {
                //关闭支付
                //关闭支付---调用pay服务实现
                Result result = wxPayFeign.closeOrder(outTradeNo);
                Map<String,String> closeMap = (Map<String, String>) result.getData();
                if(closeMap!=null && closeMap.get("return_code").equalsIgnoreCase("success") &&
                        closeMap.get("result_code").equalsIgnoreCase("success") ){
                //支付失败，将修改订单状态，存入数据库，库存回滚，删除订单信息，删除用户排队信息
                seckillOrderService.deleteSeckillOrder(username);
                }
            }

        }
    }


}
