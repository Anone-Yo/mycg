package com.anone.content.feign;
import com.anone.content.domain.ContentCategory;
import com.github.pagehelper.PageInfo;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/****
 * @Author:dao
 * @Description:
 *****/
@FeignClient(name="contentCategory")
@RequestMapping("/contentCategory")
public interface ContentCategoryFeign {

    /***
     * ContentCategory分页条件搜索实现
     * @param contentCategory
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) ContentCategory contentCategory, @PathVariable("page") int page, @PathVariable("size")  int size);

    /***
     * ContentCategory分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable("page")  int page, @PathVariable("size")  int size);

    /***
     * 多条件搜索品牌数据
     * @param contentCategory
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<ContentCategory>> findList(@RequestBody(required = false) ContentCategory contentCategory);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable Long id);

    /***
     * 修改ContentCategory数据
     * @param contentCategory
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody ContentCategory contentCategory,@PathVariable("id") Long id);

    /***
     * 新增ContentCategory数据
     * @param contentCategory
     * @return
     */
    @PostMapping
    Result add(@RequestBody ContentCategory contentCategory);

    /***
     * 根据ID查询ContentCategory数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<ContentCategory> findById(@PathVariable("id") Long id);

    /***
     * 查询ContentCategory全部数据
     * @return
     */
    @GetMapping
    Result<List<ContentCategory>> findAll();
}