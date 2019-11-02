package com.anone.order.controller;

import com.anone.order.domain.OrderItem;
import com.anone.order.service.CartService;
import entity.Result;
import entity.StatusCode;
import entity.TokenDecode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 购物车控制层
 */
@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @Autowired
    private TokenDecode tokenDecode;
    /**
     * 添加购物车
     */
    @GetMapping("/add")
    public Result addCart(Integer num,Long spuId) {
        //String username="anone";
        //用户登录后 从头文件中获取信息
       String username = tokenDecode.getUserInfo().get("username");
        cartService.addCart(num,spuId,username);
        return new Result(true, StatusCode.OK,"添加成功");
    }


    /**
     * 查询购物车列表
     */

    @GetMapping("/list")
    public Result<List<OrderItem>> list() {
        //String username="anone";
       String username = tokenDecode.getUserInfo().get("username");
        List<OrderItem> orderItems = cartService.list(username);
        return new Result<List<OrderItem>>(true,StatusCode.OK,"查询成功",orderItems);
    }
}
