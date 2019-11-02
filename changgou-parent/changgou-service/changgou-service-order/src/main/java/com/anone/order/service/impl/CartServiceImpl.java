package com.anone.order.service.impl;

import com.anone.goods.domain.Sku;
import com.anone.goods.domain.Spu;
import com.anone.goods.feign.SkuFeign;
import com.anone.goods.feign.SpuFeign;
import com.anone.order.domain.OrderItem;
import com.anone.order.service.CartService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 购物车的服务 实现类
 */
@Service
public class CartServiceImpl implements CartService {
    //远程调用sku
     @Autowired
     private SkuFeign skuFeign;
     //远程调用spu
     @Autowired
     private SpuFeign spuFeign;
     //调用redis的对象
     @Autowired
     private RedisTemplate redisTemplate;



    /**
     * 新增购物车
     * 将购物车信息存入redis中
     * @param num
     * @param skuId
     * @param username
     */
    @Override
    public void addCart(Integer num, Long skuId, String username) {
        //查看商品的详情
        //判断商品的数量是否为负数或者为0
        if (num <= 0) {
            //如果传入的商品数量是小于，那么就删除该购物车
            redisTemplate.boundHashOps("cart_"+username).delete(skuId);
            return;
        }

        //根据skuid 获取sku
        Result<Sku> skuResult = skuFeign.findById(skuId);
        //判断SkuResult是否为空
        if (skuResult != null && skuResult.isFlag()) {
            Sku sku = skuResult.getData();
            //根据sku 获取spuId 来查询spu
            Spu spu = spuFeign.findById(sku.getSpuId()).getData();
            //将sku、spu 相关的信息封装到OrderItem对象中
            OrderItem orderItem=new OrderItem();
            orderItem.setCategoryId1(spu.getCategory1Id());
            orderItem.setCategoryId2(spu.getCategory2Id());
            orderItem.setCategoryId3(spu.getCategory3Id());
            orderItem.setSpuId(spu.getId());
            orderItem.setSkuId(skuId);
            orderItem.setName(sku.getName());
            orderItem.setNum(num);
            orderItem.setImage(sku.getImage());
            orderItem.setPrice(sku.getPrice());
            orderItem.setMoney(num*sku.getPrice());
            //将username作为namespace 将OrderItem信息存入
            //一对多的关系：使用hash类型
            redisTemplate.boundHashOps("cart_"+username).put(skuId,orderItem);
        }


    }

    /**
     * 从redis中获取购物车列表信息
     * @param username
     * @return
     */
    @Override
    public List<OrderItem> list(String username) {
        List<OrderItem> orderItems = redisTemplate.boundHashOps("cart_"+username).values();
        return orderItems;
    }
}
