package com.anone.goods.dao;
import com.anone.goods.domain.Template;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:anone
 * @Description:Template的Dao
 *****/
@Component
public interface TemplateMapper extends Mapper<Template> {
}
