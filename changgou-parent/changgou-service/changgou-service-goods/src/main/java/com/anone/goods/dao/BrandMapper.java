package com.anone.goods.dao;
import com.anone.goods.domain.Brand;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/****
 * @Author:anone
 * @Description:Brand的Dao
 *****/
@Component
public interface BrandMapper extends Mapper<Brand> {
    /**
     * 根据categoryid查询品牌信息
     */
    @Select("select tb.* from tb_brand tb,tb_category_brand tcb where tcb.brand_id=tb.id and tcb.category_id=#{categoryId}")
    List<Brand> findBrandByCategoryId(Integer categoryId);
}
