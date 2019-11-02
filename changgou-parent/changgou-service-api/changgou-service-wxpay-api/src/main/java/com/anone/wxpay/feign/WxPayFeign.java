package com.anone.wxpay.feign;

import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient
@RequestMapping("/weixin/pay")
public interface WxPayFeign {
    /**
     * 关闭订单
     */
    @GetMapping(value = "/close/order")
    public Result closeOrder(String outtradeno);
}
