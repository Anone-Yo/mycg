package com.anone.wxPay.controller;

import com.anone.wxPay.service.WxPayService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping(value = "/wx/pay")
public class WxPayController {
    @Autowired
    private WxPayService wxPayService;

    /***
     * 创建二维码
     * @return
     */
    @RequestMapping(value = "/ct/native")
    public Result createNative(@RequestParam Map<String,String> parameterMap){
        //创建二维码
        Map<String,String> resultMap = wxPayService.createnative(parameterMap);
        return new Result(true, StatusCode.OK,"创建二维码预付订单成功！",resultMap);
    }
}
