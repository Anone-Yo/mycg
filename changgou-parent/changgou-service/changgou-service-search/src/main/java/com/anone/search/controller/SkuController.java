package com.anone.search.controller;

import com.anone.search.service.SkuService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/search")
@CrossOrigin
public class SkuController {
    @Autowired
    private SkuService skuService;

    @GetMapping("/import")
    public Result importData() {
        skuService.importData();
        return new Result(true, StatusCode.OK,"导入数据成功");
    }

    @GetMapping
    public Map searchData(@RequestParam(required = false) Map<String,String> searchMap) throws Exception{
        Map<String, Object> map = skuService.searchData(searchMap);
        return map;
    }

}
