package com.anone.item.service;

public interface PageService {
    /**
     * 根据商品spuid 生成静态页面
     */
    public void createPageHtml(Long spuId) ;
}
