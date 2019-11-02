package com.anone.wxPay.service;

import java.util.Map;

public interface WeiXinPayService {
    /**
     * 创建二维码
     * 订单号outtradeno
     * 支付的金额 totalfee
     * @param dataMap
     * @return
     */
    Map createNative(Map<String,String> dataMap);

    /**
     * 查询支付状态
     * 订单号 outtradeno
     */
    Map queryPayStatus(String outtradeno);

    /**
     *关闭订单
     */
    Map closeOrder(String outtradeno);
}
