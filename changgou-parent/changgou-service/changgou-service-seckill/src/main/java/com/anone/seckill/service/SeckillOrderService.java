package com.anone.seckill.service;
import com.anone.seckill.domain.SeckillOrder;
import com.anone.seckill.domain.SeckillStatus;
import com.github.pagehelper.PageInfo;
import java.util.List;
/****
 * @Author:anone
 * @Description:SeckillOrder业务层接口
 *****/
public interface SeckillOrderService {
    /**
     * 修改订单的状态
     * 支付时间
     * 流水号
     * 支付状态
     * 用户名
     */
    void updateSeckillOrderStatus(String endtime,String transactionId,String username);

    /**
     * 删除订单
     */
    void deleteSeckillOrder(String username);

    /***
     * 抢单状态查询
     * @param username
     */
    SeckillStatus queryStatus(String username);

    /***
     * 秒杀下单
     * @param dateMeun
     * @param id
     * @param username
     */
    Boolean add(String dateMeun, Long id, String username);

    /***
     * SeckillOrder多条件分页查询
     * @param seckillOrder
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size);

    /***
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<SeckillOrder> findPage(int page, int size);

    /***
     * SeckillOrder多条件搜索方法
     * @param seckillOrder
     * @return
     */
    List<SeckillOrder> findList(SeckillOrder seckillOrder);

    /***
     * 删除SeckillOrder
     * @param id
     */
    void delete(Long id);

    /***
     * 修改SeckillOrder数据
     * @param seckillOrder
     */
    void update(SeckillOrder seckillOrder);

    /***
     * 新增SeckillOrder
     * @param seckillOrder
     */
    void add(SeckillOrder seckillOrder);

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
     SeckillOrder findById(Long id);

    /***
     * 查询所有SeckillOrder
     * @return
     */
    List<SeckillOrder> findAll();
}
