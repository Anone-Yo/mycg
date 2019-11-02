package com.anone.seckill.mq.listener;

import com.alibaba.fastjson.JSON;
import com.anone.seckill.domain.SeckillOrder;
import com.anone.seckill.domain.SeckillStatus;
import com.anone.seckill.service.SeckillOrderService;
import com.anone.wxpay.feign.WxPayFeign;
import entity.Result;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 监听延迟队列消息
 */
@Component
@RabbitListener(queues = "seckillQueue")
public class DelaySeckillListener {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private SeckillOrderService seckillOrderService;
    @Autowired
    private WxPayFeign wxPayFeign;

    @RabbitHandler
    public void getMessage(String message) {
        //将json字符串转换成对象
        SeckillStatus seckillStatus = JSON.parseObject(message, SeckillStatus.class);
        //从redis中获取用户的排队的信息，如果还有排队信息，则说明订单超时，未支付
        Object UserQueueStatus = redisTemplate.boundHashOps("UserQueueStatus").get(seckillStatus.getUsername());
        if (UserQueueStatus != null) {
            SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(seckillStatus.getUsername());
            //关闭支付---调用pay服务实现
            Result result = wxPayFeign.closeOrder(String.valueOf(seckillOrder.getId()));
            Map<String,String> closeMap = (Map<String, String>) result.getData();
            if(closeMap!=null && closeMap.get("return_code").equalsIgnoreCase("success") &&
                    closeMap.get("result_code").equalsIgnoreCase("success") ) {
                //取消订单+回滚订单
                seckillOrderService.deleteSeckillOrder(seckillStatus.getUsername());
            }
        }


    }
}
