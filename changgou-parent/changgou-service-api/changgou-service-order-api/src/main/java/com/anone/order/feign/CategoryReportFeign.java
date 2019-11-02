package com.anone.order.feign;
import com.anone.order.domain.CategoryReport;
import com.github.pagehelper.PageInfo;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/****
 * @Author:anone
 * @Description:
 *****/
@FeignClient(name="categoryReport")
@RequestMapping("/categoryReport")
public interface CategoryReportFeign {

    /***
     * CategoryReport分页条件搜索实现
     * @param categoryReport
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) CategoryReport categoryReport, @PathVariable("page")   int page, @PathVariable("size")   int size);

    /***
     * CategoryReport分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable("page")   int page, @PathVariable("size")   int size);

    /***
     * 多条件搜索品牌数据
     * @param categoryReport
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<CategoryReport>> findList(@RequestBody(required = false) CategoryReport categoryReport);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable("id") Date id);

    /***
     * 修改CategoryReport数据
     * @param categoryReport
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody CategoryReport categoryReport,@PathVariable("id")  Date id);

    /***
     * 新增CategoryReport数据
     * @param categoryReport
     * @return
     */
    @PostMapping
    Result add(@RequestBody CategoryReport categoryReport);

    /***
     * 根据ID查询CategoryReport数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<CategoryReport> findById(@PathVariable("id")  Date id);

    /***
     * 查询CategoryReport全部数据
     * @return
     */
    @GetMapping
    Result<List<CategoryReport>> findAll();
}