package com.anone.user.feign;
import com.anone.user.domain.User;
import com.github.pagehelper.PageInfo;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:anone
 * @Description:
 *****/
@FeignClient(name="user")
@RequestMapping("/user")
public interface UserFeign {
    /**
     * 增加积分
     * 注意：需要@RequestParam注解，否则无法接收数据
     */
    @GetMapping(value = "/points/add")
    public Result addUserPoint(@RequestParam Integer points);

    /***
     * User分页条件搜索实现
     * @param user
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) User user, @PathVariable("page")   int page, @PathVariable("size")   int size);

    /***
     * User分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable("page")   int page, @PathVariable("size")   int size);

    /***
     * 多条件搜索品牌数据
     * @param user
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<User>> findList(@RequestBody(required = false) User user);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable("id")  String id);

    /***
     * 修改User数据
     * @param user
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody User user,@PathVariable("id")  String id);

    /***
     * 新增User数据
     * @param user
     * @return
     */
    @PostMapping
    Result add(@RequestBody User user);

    /***
     * 根据ID查询User数据
     * @param id
     * @return
     */
    @GetMapping(value ="/load/{id}")
    Result<User> findById(@PathVariable("id")  String id);

    /***
     * 查询User全部数据
     * @return
     */
    @GetMapping
    Result<List<User>> findAll();
}