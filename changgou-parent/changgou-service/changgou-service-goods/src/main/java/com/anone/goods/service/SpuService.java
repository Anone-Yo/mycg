package com.anone.goods.service;

import com.anone.goods.domain.Goods;
import com.anone.goods.domain.Spu;
import com.github.pagehelper.PageInfo;

import java.util.List;
/****
 * @Author:anone
 * @Description:Spu业务层接口
 *****/
public interface SpuService {
    /**
     * 商品审核
     */
    void audit(Long spuId);
    /**
     * 商品下架
     */
    void pull(Long spuId);
    /**
     * 商品上架
     */
    void put(Long spuId);
    /**
     * 批量上架
     */
    int putMany(Long[] ids);
    /**
     * 批量下架
     */
    int pullMany(Long[] ids);
    /**
     * 逻辑删除商品
     * 判断商品是否上架
     */
    void  logicDelete(Long spuId);
    /**
     * 还原被删除的商品
     */
    void  restore(Long spuId);


    /**
     * 根据spu id查询商品
     */
    Goods findGoods(long spuId);

    /**
     * 新增商品
     * Goods ==>spu（公共） sku（特有）
     */
    void addGoods(Goods goods);

    /***
     * Spu多条件分页查询
     * @param spu
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(Spu spu, int page, int size);

    /***
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Spu> findPage(int page, int size);

    /***
     * Spu多条件搜索方法
     * @param spu
     * @return
     */
    List<Spu> findList(Spu spu);

    /***
     * 删除Spu
     * @param id
     */
    void delete(Long id);

    /***
     * 修改Spu数据
     * @param spu
     */
    void update(Spu spu);

    /***
     * 新增Spu
     * @param spu
     */
    void add(Spu spu);

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
     Spu findById(Long id);

    /***
     * 查询所有Spu
     * @return
     */
    List<Spu> findAll();
}
