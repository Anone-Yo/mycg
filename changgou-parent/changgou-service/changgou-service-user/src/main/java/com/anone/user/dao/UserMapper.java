package com.anone.user.dao;
import com.anone.user.domain.User;
import feign.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

/****
 * @Author:anone
 * @Description:User的Dao
 *****/
public interface UserMapper extends Mapper<User> {

    /**
     * 修改用户的积分
     * 使用行级锁实现
     * @param username
     * @param points
     * @return
     */
    @Update("update tb_user set points=points+#{points} where username=#{username}")
    int addUserPoint(@Param("username") String username, @Param("points") Integer points);
}
