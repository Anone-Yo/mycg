package com.anone.order.mq.listener;

import com.alibaba.fastjson.JSON;
import com.anone.goods.feign.SkuFeign;
import com.anone.order.dao.OrderItemMapper;
import com.anone.order.dao.OrderMapper;
import com.anone.order.domain.Order;
import com.anone.order.domain.OrderItem;
import com.anone.order.service.OrderService;
import com.anone.wxpay.feign.WxPayFeign;
import entity.Result;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *延时队列的监听
 */
@Component
@RabbitListener(queues = "orderListenerQueue")
public class delayMessageListener {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private OrderService orderService;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private WxPayFeign wxPayFeign;

    @RabbitHandler
    public void getMessage(String message) {
            //获取订单号
        Order order = orderMapper.selectByPrimaryKey(message);
        //获取订单的支付状态
        String payStatus = order.getPayStatus();
        //支付失败，说明未支付，支付失败
        if (payStatus.equals("0")) {
            //取消订单===》逻辑删除订单
            orderService.logicDelete(message);
            //关闭支付
            Result result = wxPayFeign.closeOrder(message);
            Map<String,String> closeMap = (Map<String, String>) result.getData();
            if (closeMap != null && closeMap.get("return_code").equalsIgnoreCase("success") &&
                    closeMap.get("result_code").equalsIgnoreCase("success")) {
                //库存回滚
                List<Long> skuIds = order.getSkuIds();
                for (Long skuId : skuIds) {
                    OrderItem orderItem=new OrderItem();
                    orderItem.setSkuId(skuId);
                    orderItem.setOrderId(message);
                    List<OrderItem> orderItems = orderItemMapper.select(orderItem);
                    for (OrderItem item : orderItems) {
                        skuFeign.rollCount(skuId,item.getNum());
                    }
                }
            }


        }


    }
}
