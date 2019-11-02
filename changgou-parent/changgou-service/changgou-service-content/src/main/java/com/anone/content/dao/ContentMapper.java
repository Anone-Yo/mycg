package com.anone.content.dao;
import com.anone.content.domain.Content;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:anone
 * @Description:Content的Dao
 *****/
@Component
public interface ContentMapper extends Mapper<Content> {
}
