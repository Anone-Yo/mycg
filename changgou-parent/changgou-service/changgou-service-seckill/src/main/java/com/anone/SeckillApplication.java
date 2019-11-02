package com.anone;

import entity.IdWorker;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableScheduling //开启定时任务
@EnableFeignClients(basePackages = "com.anone.wxpay.feign")
@EnableRabbit
@MapperScan(basePackages = "com.anone.seckill.dao")
public class SeckillApplication {
    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class,args);
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(0,0);
    }
}
