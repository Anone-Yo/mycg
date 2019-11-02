package com.anone.item.controller;

import com.anone.item.service.PageService;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 生成静态详情页面
 */
@RestController
@RequestMapping("/page")
public class PageController {
    @Autowired
    private PageService pageService;

    @GetMapping("/createHtml/{id}")
    public Result createHtml(@PathVariable("id") Long spuId) {
        pageService.createPageHtml(spuId);
        return new Result(true, StatusCode.OK,"生成静态页面成功");
    }
}
