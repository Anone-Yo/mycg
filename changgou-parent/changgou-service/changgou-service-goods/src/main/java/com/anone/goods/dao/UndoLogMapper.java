package com.anone.goods.dao;
import com.anone.goods.domain.UndoLog;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:anone
 * @Description:UndoLog的Dao
 *****/
@Component
public interface UndoLogMapper extends Mapper<UndoLog> {
}
