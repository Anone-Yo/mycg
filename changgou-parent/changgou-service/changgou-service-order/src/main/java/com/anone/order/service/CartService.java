package com.anone.order.service;

import com.anone.order.domain.OrderItem;

import java.util.List;

/**
 * 购物车的服务 接口
 */
public interface CartService {
    /**
     *增加购物车
     * 需要的条件：
     * 1.商品的数量num
     * 2.商品的sku id
     * 3.用户登录，还需要用户名
     */
   void addCart(Integer num,Long skuId,String username);

    /**
     * 获取购物车列表
     */
    List<OrderItem> list(String username);
}
