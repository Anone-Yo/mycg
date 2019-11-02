package com.anone.wxPay.service;

import java.util.Map;

public interface WxPayService {

    /***
     * 创建二维码操作
     */
    Map createnative(Map<String,String> parameterMap);
}
