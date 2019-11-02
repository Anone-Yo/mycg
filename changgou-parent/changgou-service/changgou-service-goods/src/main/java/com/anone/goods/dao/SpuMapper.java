package com.anone.goods.dao;
import com.anone.goods.domain.Spu;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:anone
 * @Description:Spu的Dao
 *****/
@Component
public interface SpuMapper extends Mapper<Spu> {
}
