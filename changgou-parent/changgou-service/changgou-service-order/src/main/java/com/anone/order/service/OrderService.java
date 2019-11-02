package com.anone.order.service;

import com.anone.order.domain.Order;
import com.github.pagehelper.PageInfo;

import java.text.ParseException;
import java.util.List;
/****
 * @Author:anone
 * @Description:Order业务层接口
 *****/
public interface OrderService {
    /**
     * 修改订单的状态
     * 修改时间
     * 订单号
     * 微信流水号
     */
    void updateStatus(String outtradeno, String payTime, String transactionId) throws ParseException, Exception;

    /**
     *修改订单状态，逻辑删除，将状态该为删除
     */
    void logicDelete(String outtradeno);


    /***
     * Order多条件分页查询
     * @param order
     * @param page
     * @param size
     * @return
     */
    PageInfo<Order> findPage(Order order, int page, int size);

    /***
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    PageInfo<Order> findPage(int page, int size);

    /***
     * Order多条件搜索方法
     * @param order
     * @return
     */
    List<Order> findList(Order order);

    /***
     * 删除Order
     * @param id
     */
    void delete(String id);

    /***
     * 修改Order数据
     * @param order
     */
    void update(Order order);

    /***
     * 新增Order
     * @param order
     */
    void add(Order order);

    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
     Order findById(String id);

    /***
     * 查询所有Order
     * @return
     */
    List<Order> findAll();
}
