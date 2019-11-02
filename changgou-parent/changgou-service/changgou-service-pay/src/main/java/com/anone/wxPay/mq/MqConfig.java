package com.anone.wxPay.mq;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * rabbitMq 配置
 */
@Configuration
public class MqConfig {
    //注入Environment对象--->从配置文件中读取数据
    @Autowired
    private Environment env;

    //创建队列
    @Bean
    public Queue orderQueue() {
        return new Queue(env.getProperty("mq.pay.queue.order"));
    }

    //创建交换机
    @Bean
    public Exchange orderExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.order"));
    }

    //队列绑定交换机
    @Bean
    public Binding binding(Queue orderQueue,Exchange orderExchange) {
        return BindingBuilder.bind(orderQueue)
                .to(orderExchange)
                .with(env.getProperty("mq.pay.routing.key"))
                .noargs();
    }

    /***********************秒杀队列**********************************/
    //创建队列
    @Bean
    public Queue orderSekillQueue() {
        return new Queue(env.getProperty("mq.pay.queue.seckillorder"));
    }

    //创建交换机
    @Bean
    public Exchange orderSekillExchange() {
        return new DirectExchange(env.getProperty("mq.pay.exchange.seckillorder"));
    }

    //队列绑定交换机
    @Bean
    public Binding bindingSekill(Queue orderSekillQueue,Exchange orderSekillExchange) {
        return BindingBuilder.bind(orderSekillQueue)
                .to(orderSekillExchange)
                .with(env.getProperty("mq.pay.routing.seckillkey"))
                .noargs();
    }
}
