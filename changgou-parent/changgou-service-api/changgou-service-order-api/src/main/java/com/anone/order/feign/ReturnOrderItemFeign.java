package com.anone.order.feign;
import com.anone.order.domain.ReturnOrderItem;
import com.github.pagehelper.PageInfo;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/****
 * @Author:anone
 * @Description:
 *****/
@FeignClient(name="returnOrderItem")
@RequestMapping("/returnOrderItem")
public interface ReturnOrderItemFeign {

    /***
     * ReturnOrderItem分页条件搜索实现
     * @param returnOrderItem
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@RequestBody(required = false) ReturnOrderItem returnOrderItem, @PathVariable("page")   int page, @PathVariable("size")   int size);

    /***
     * ReturnOrderItem分页搜索实现
     * @param page:当前页
     * @param size:每页显示多少条
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    Result<PageInfo> findPage(@PathVariable("page")   int page, @PathVariable("size")   int size);

    /***
     * 多条件搜索品牌数据
     * @param returnOrderItem
     * @return
     */
    @PostMapping(value = "/search" )
    Result<List<ReturnOrderItem>> findList(@RequestBody(required = false) ReturnOrderItem returnOrderItem);

    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}" )
    Result delete(@PathVariable("id")  Long id);

    /***
     * 修改ReturnOrderItem数据
     * @param returnOrderItem
     * @param id
     * @return
     */
    @PutMapping(value="/{id}")
    Result update(@RequestBody ReturnOrderItem returnOrderItem,@PathVariable("id")  Long id);

    /***
     * 新增ReturnOrderItem数据
     * @param returnOrderItem
     * @return
     */
    @PostMapping
    Result add(@RequestBody ReturnOrderItem returnOrderItem);

    /***
     * 根据ID查询ReturnOrderItem数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<ReturnOrderItem> findById(@PathVariable("id")  Long id);

    /***
     * 查询ReturnOrderItem全部数据
     * @return
     */
    @GetMapping
    Result<List<ReturnOrderItem>> findAll();
}