package com.anone.search.service;


import java.util.Map;

public interface SkuService {
    /**
     * 将数据导入ES
     */
    void importData();

    /**
     * 查询页面分类的信息
     * 包括：商品信息
     * 分类
     * 品牌
     * 参数.....
     * 返回一个map
     */
    Map<String,Object> searchData(Map<String,String> searchMap) throws Exception;


}
