package com.anone;

import entity.FeignInterceptor;
import entity.IdWorker;
import entity.TokenDecode;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@EnableRabbit
@MapperScan(basePackages = {"com.anone.order.dao"})
@EnableFeignClients(basePackages = {"com.anone.goods.feign","com.anone.user.feign","com.anone.wxpay.feign"})
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }

    /**
     * 创建解析令牌 获取用户信息的对象
     * @return
     */
    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }

    /**
     * 创建idworker 使用唯一的id
     */
    @Bean
    public IdWorker idWorker() {
        return new IdWorker(0,0);
    }

    /**
     * 创建拦截器，封装请求头信息
     */
    @Bean
    public FeignInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }


}
