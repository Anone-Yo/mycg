package com.anone.user.feign;
import com.anone.user.domain.Address;
import com.github.pagehelper.PageInfo;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:anone
 * @Description:
 *****/
@FeignClient(name="address")
@RequestMapping("/address")
public interface AddressFeign {

    /***
     * Address分页条件搜索实现
     * @param address
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) Address address, @PathVariable("page")   int page, @PathVariable("size")   int size);

    /***
     * Address分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable("page")   int page, @PathVariable("size")   int size);

    /***
     * 多条件搜索品牌数据
     * @param address
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<Address>> findList(@RequestBody(required = false) Address address);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable("id")  Integer id);

    /***
     * 修改Address数据
     * @param address
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody Address address,@PathVariable("id")  Integer id);

    /***
     * 新增Address数据
     * @param address
     * @return
     */
    @PostMapping
    Result add(@RequestBody Address address);

    /***
     * 根据ID查询Address数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Address> findById(@PathVariable("id")  Integer id);

    /***
     * 查询Address全部数据
     * @return
     */
    @GetMapping
    Result<List<Address>> findAll();
}