package com.anone.goods.feign;

import com.anone.goods.domain.Sku;
import entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(value = "goods")
@RequestMapping("/sku")
public interface SkuFeign {
    /**
     * 库存回滚
     */
    @RequestMapping("/roll/count")
    public Result rollCount(@RequestParam("id") Long id,@RequestParam("num") Integer num);

    /***
     * 查询Sku全部数据
     * @return
     */
    @GetMapping
    Result<List<Sku>> findAll();

    /**
     * 根据条件搜索
     * @param sku
     * @return
     */
    @PostMapping(value = "/search" )
    public Result<List<Sku>> findList(@RequestBody(required = false) Sku sku);

    /***
     * 根据ID查询SKU信息
     * @param id : sku的ID
     */
    @GetMapping(value = "/{id}")
    public Result<Sku> findById(@PathVariable(value = "id", required = true) Long id);

    /**
     * 库存递减
     * @param decrMap
     * @return
     */
    @GetMapping(value = "/decr/count")
    public Result decrMap(@RequestParam Map decrMap);
}