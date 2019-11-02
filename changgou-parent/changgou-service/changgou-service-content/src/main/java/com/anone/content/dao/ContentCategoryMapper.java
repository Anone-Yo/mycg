package com.anone.content.dao;
import com.anone.content.domain.ContentCategory;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:anone
 * @Description:ContentCategory的Dao
 *****/
@Component
public interface ContentCategoryMapper extends Mapper<ContentCategory> {
}
