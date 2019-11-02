package com.anone.seckill.service;
import com.anone.seckill.domain.SeckillGoods;
import com.github.pagehelper.PageInfo;
import java.util.List;
/****
 * @Author:anone
 * @Description:SeckillGoods业务层接口
 *****/
public interface SeckillGoodsService {
    /**
     * 查询秒杀商品的详情页
     * 时间区间
     *秒杀商品的id
     */
    SeckillGoods one(String dateMeun,Long id);

    /**
     * 查询秒杀频道页
     */
    List<SeckillGoods> list(String dateMeun);

    /***
     * SeckillGoods多条件分页查询
     * @param seckillGoods
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillGoods> findPage(SeckillGoods seckillGoods, int page, int size);

    /***
     * SeckillGoods分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillGoods> findPage(int page, int size);

    /***
     * SeckillGoods多条件搜索方法
     * @param seckillGoods
     * @return
     */
    List<SeckillGoods> findList(SeckillGoods seckillGoods);

    /***
     * 删除SeckillGoods
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillGoods数据
     * @param seckillGoods
     */
    void update(SeckillGoods seckillGoods);

    /***
     * 新增SeckillGoods
     * @param seckillGoods
     */
    void add(SeckillGoods seckillGoods);

    /**
     * 根据ID查询SeckillGoods
     * @param id
     * @return
     */
     SeckillGoods findById(Long id);

    /***
     * 查询所有SeckillGoods
     * @return
     */
    List<SeckillGoods> findAll();
}
