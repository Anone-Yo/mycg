package com.anone.seckill.mq.queue;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 创建延时队列
 */
@Configuration
public class QueueConfig {

    //创建队列一  延时队列
    @Bean
    public Queue delaySeckillOrderQueue() {
        return QueueBuilder.durable("delaySeckillOrderQueue")
                .withArgument("x-dead-letter-exchange","seckillExchange")//绑定死信交换机
                .withArgument("x-dead-letter-routing-key","seckillQueue")//绑定过期消息发送的路由地址
                .build();
    }

    //创建队列二   正常监听的队列
    @Bean
    public Queue seckillQueue() {
        return new Queue("seckillQueue");
    }

    //创建交换机
    @Bean
    public Exchange seckillExchange() {
        return new DirectExchange("seckillExchange");
    }

    //绑定队列和交换机
    @Bean
    public Binding bindingSeckill(Queue seckillQueue,Exchange seckillExchange) {
        return BindingBuilder.bind(seckillQueue).to(seckillExchange).with("seckillQueue").noargs();
    }


}
