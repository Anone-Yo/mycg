package com.anone.goods.dao;

import com.anone.goods.domain.Sku;
import feign.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:anone
 * @Description:Sku的Dao
 *****/
@Component
public interface SkuMapper extends Mapper<Sku> {
    /**
     * 库存递减
     * 修改商品表的数据==>行级锁：保持事务的原子性
     */
    @Update("update tb_sku set num=num-#{num} where id=#{skuId} and num>=#{num} ")
    int decrCount(@Param("skuId")Long skuId, @Param("num") Integer num);
}
