package com.anone.goods.dao;
import com.anone.goods.domain.Category;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:anone
 * @Description:Category的Dao
 *****/
@Component
public interface CategoryMapper extends Mapper<Category> {
}
