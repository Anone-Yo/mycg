package com.anone.search.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "search")
@RequestMapping("/search")
public interface SkuFeign {

    @GetMapping
    public Map searchData(@RequestParam(required = false) Map<String,String> searchMap) throws Exception;
}
