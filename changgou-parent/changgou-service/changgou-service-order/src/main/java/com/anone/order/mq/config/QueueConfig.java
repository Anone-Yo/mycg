package com.anone.order.mq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * 两个队列
 * 一个延时队列 会过期，过期后会把监听到数据传递给第二个队列
 * 一个正常的消息队列
 * 延时队列的配置
 */
@Component
public class QueueConfig {
    //Queue1  延时队列 会过期
    @Bean
    public Queue orderDelayQueue() {
        //延时队列创建,绑定orderDelayQueue
        return QueueBuilder.durable("orderDelayQueue")
                //信息超时进入死信队列，绑定死信队列交换机，过期信息绑定交换机
                .withArgument("x-dead-letter-exchange","orderListenerExchange")
                //绑定指定的routing-key===>将监听到的数据路由到指定的队列中
                .withArgument("x-dead-letter-routing-key","orderListenerQueue")
                .build();
    }

    //Queue2 接收延时队列过期后的数据
    @Bean
    public Queue orderListenerQueue() {
        return new Queue("orderListenerQueue",true);
    }

    //创建交换机
    @Bean
    public Exchange orderListenerExchange() {

        return new DirectExchange("orderListenerExchange");
    }

    //绑定交换机和消息队列---注意：这里交换机不是与过期队列绑定
    @Bean
    public Binding binding(Queue orderListenerQueue,Exchange orderListenerExchange) {
        return BindingBuilder
                .bind(orderListenerQueue)
                .to(orderListenerExchange)
                .with("orderListenerQueue")
                .noargs();
    }

}
