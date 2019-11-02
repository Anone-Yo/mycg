package com.anone.seckill.task;

import com.alibaba.fastjson.JSON;
import com.anone.seckill.dao.SeckillGoodsMapper;
import com.anone.seckill.domain.SeckillGoods;
import com.anone.seckill.domain.SeckillOrder;
import com.anone.seckill.domain.SeckillStatus;
import entity.IdWorker;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import java.util.Date;

/***
 * 多线程创建订单操作类
 */
@Component
@EnableAsync //开启多线程-异步
public class MultiThreadingCreateOrder {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Async //开启多线程 -异步处理
    public void createOrder() {
        try {
            //从redis队列中获取用户信息、秒杀商品id 、时间分区
            //获取排队信息
            SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("seckillOrderQueue").rightPop();
            //判断是否排队信息，没有则不进行操作
            if (seckillStatus == null) {
                return;
            }

            Thread.sleep(5000);

            String username = seckillStatus.getUsername();
            String dateMeun = seckillStatus.getTime();
            Long id = seckillStatus.getGoodsId();
            String namespace = "seckillGoods_" + dateMeun;

            //下单之前
            //从list中获取商品
            Object sgoods = redisTemplate.boundListOps("SeckillGoodsCountList_" + id).rightPop();
            //判断获取到数据是否为空，如果为空，则说明没有库存
            if (sgoods == null) {
                //清空redis 中的排队信息 和查询下单状态的信息
                clearUserQueue(username);
                return;//停止继续下单操作
            }

            //查询秒杀商品
            SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(namespace).get(id);
            //判断商品是否有库存
            if (seckillGoods.getStockCount() <= 0 || seckillGoods == null) {
                throw new RuntimeException("商品已售罄");
            }
            SeckillOrder seckillOrder = new SeckillOrder();
            //封装订单信息，封装到redis 中 进行排队
            seckillOrder.setCreateTime(new Date());
            seckillOrder.setStatus("0");
            seckillOrder.setSeckillId(id);
            seckillOrder.setId(idWorker.nextId());
            seckillOrder.setMoney(seckillGoods.getCostPrice());
            seckillOrder.setUserId(username);
            //订单存入redis中
            redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);
            //库存递减
            //商品如果是最后一个，则将redis中的商品信息删除，并且将redis中的商品信息同步到MySQL
            seckillGoods.setStockCount(seckillGoods.getStockCount() - 1);
            //处理秒杀商品数据同步不精准的问题
            //获取redis中商品队列的长度
            Long size = redisTemplate.boundListOps("SeckillGoodsCountList_" + id).size();

            //if (seckillGoods.getStockCount() <= 0) {
            if (size <= 0) {
                //同步数据库
                seckillGoods.setStockCount(size.intValue());
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
                //删除redis 中的秒杀商品
                redisTemplate.boundHashOps(namespace).delete(id);
            } else {
                //如果还有库存，则同步到redis中
                redisTemplate.boundHashOps(namespace).put(id, seckillGoods);
            }
            //下单成功后，更新下单状态
            //抢单成功，更新抢单状态,排队->等待支付
            seckillStatus.setOrderId(seckillOrder.getId());
            seckillStatus.setMoney(Float.valueOf(seckillGoods.getCostPrice()));
            seckillStatus.setStatus(2); //待付款
            //存入redis中
            //将排队信息存入redis中
            redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
             //发送订单信息到延时队列中
            rabbitTemplate.convertAndSend("delaySeckillOrderQueue", (Object) JSON.toJSONString(seckillStatus), new MessagePostProcessor() {
                @Override
                public Message postProcessMessage(Message message) throws AmqpException {
                    message.getMessageProperties().setExpiration("10000");//设置延时队列的过期时间 10秒
                    return message;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        }

    //清空redis中的排队信息 和查询订单状态的信息
    public void clearUserQueue(String username) {
        //删除 防止 用户重复的排队的redis(排队标识)
        //说明：客户下单失败，就可以进行新的抢单排队，需要把之前的排队信息清空
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //删除 用户排队的信息->下单状态（下单状态）
        //说明：客户下单失败，就没有下单的状态，需要重新下单
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);
    }

}
