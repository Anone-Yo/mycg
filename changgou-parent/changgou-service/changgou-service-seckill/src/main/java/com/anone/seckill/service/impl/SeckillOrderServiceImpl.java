package com.anone.seckill.service.impl;
import com.anone.seckill.dao.SeckillGoodsMapper;
import com.anone.seckill.dao.SeckillOrderMapper;
import com.anone.seckill.domain.SeckillGoods;
import com.anone.seckill.domain.SeckillOrder;
import com.anone.seckill.domain.SeckillStatus;
import com.anone.seckill.service.SeckillGoodsService;
import com.anone.seckill.service.SeckillOrderService;
import com.anone.seckill.task.MultiThreadingCreateOrder;
import com.anone.wxpay.feign.WxPayFeign;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.DateUtil;
import entity.IdWorker;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
/****
 * @Author:anone
 * @Description:SeckillOrder业务层接口实现类
 *****/
@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;
    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;


    /**
     * 修改订单的状态
     * 支付时间
     * 流水号
     * 支付状态
     * 用户名
     */
    @Override
    public void updateSeckillOrderStatus(String endtime, String transactionId,String username) {
        //从redis中获取客户的订单信息
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //判断秒杀订单是否为空
        if (seckillOrder != null) {
            try {
                //支付成功，需要修改订单的信息
                seckillOrder.setStatus("1");
                seckillOrder.setTransactionId(transactionId);
                seckillOrder.setUserId(username);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                Date endTimeInfo = simpleDateFormat.parse(endtime);
                seckillOrder.setPayTime(endTimeInfo);
                //修改
                seckillOrderMapper.updateByPrimaryKeySelective(seckillOrder);
                //删除订单信息
                redisTemplate.boundHashOps("SeckillOrder").delete(username);
                //清理用户排队信息、下单状态 和订单信息
                clearQueue(username);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 删除订单+ 库存回滚
     * @param username
     */
    @Override
    public void deleteSeckillOrder(String username) {
            //从redis中获取订单数据
        SeckillOrder seckillOrder = (SeckillOrder) redisTemplate.boundHashOps("SeckillOrder").get(username);
        //设置订单状态为未支付
        seckillOrder.setStatus("0");
        //存入数据库中
        seckillOrderMapper.insertSelective(seckillOrder);
        //查询用户的排队信息===》作用：获取时间分区、获取商品的id
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        //清除订单信息
        redisTemplate.boundHashOps("SeckillOrder").delete(username);
        //请求用户排队的信息
        clearQueue(username);
        //库存回滚
         String namespace="seckillGoods_"+seckillStatus.getTime();
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(namespace).get(seckillStatus.getGoodsId());

        //判断秒杀商品是否为空
        //为空，则说明没说商品
        if (seckillGoods == null) {
            //从数据库中获取
            seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillStatus.getGoodsId());
            //回滚库存从0变1 +1
            seckillGoods.setStockCount(1);
            seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);
        } else {
            //redis中有数据
            seckillGoods.setStockCount(seckillGoods.getStockCount()+1);
        }
        //修改redis中的数据
        redisTemplate.boundHashOps(namespace).put(seckillGoods.getId(),seckillGoods);

        //将商品存入队列中
        redisTemplate.boundListOps("SeckllGoodsList_"+seckillGoods.getId()).leftPush(seckillGoods.getId());
    }

    /***‘
     * 清除用户排队信息
     */
    public void clearQueue(String username) {
        //清除排队信息
        redisTemplate.boundHashOps("UserQueueCount").delete(username);
        //清除下单状态信息
        redisTemplate.boundHashOps("UserQueueStatus").delete(username);


    }

    /***
     * 抢单状态查询
     * @param username
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        //从redis 中获取 用户的订单状态
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
        return seckillStatus;
    }

    /***
     * 秒杀下单---进行排队
     * @param dateMeun
     * @param id
     * @param username
     */
    @Override
    public Boolean add(String dateMeun, Long id, String username) {
        //防止重复排队
        //判断是否已经排队了---redis的incr自增实现---redis是单线程的
        Long increment = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        //判断increment是否递增
        if (increment >1) {
            throw new RuntimeException(String.valueOf(StatusCode.REPERROR));
        }


        //创建排队对象
        SeckillStatus seckillStatus=new SeckillStatus();
        seckillStatus.setCreateTime(new Date());
        seckillStatus.setUsername(username);
        seckillStatus.setGoodsId(id);
        //秒杀状态  1:排队中，2:秒杀等待支付,3:支付超时，4:秒杀失败,5:支付完成
        seckillStatus.setStatus(1);
        //时间分区
        seckillStatus.setTime(dateMeun);
        //使用List 类型 ，用户进行排队抢单
        redisTemplate.boundListOps("seckillOrderQueue").leftPush(seckillStatus);

        //多线程抢单操作
        multiThreadingCreateOrder.createOrder();
        return true;
    }

    /**
     * SeckillOrder条件+分页查询
     * @param seckillOrder 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(seckillOrder);
        //执行搜索
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectByExample(example));
    }

    /**
     * SeckillOrder分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<SeckillOrder> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<SeckillOrder>(seckillOrderMapper.selectAll());
    }

    /**
     * SeckillOrder条件查询
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder){
        //构建查询条件
        Example example = createExample(seckillOrder);
        //根据构建的条件查询数据
        return seckillOrderMapper.selectByExample(example);
    }


    /**
     * SeckillOrder构建查询对象
     * @param seckillOrder
     * @return
     */
    public Example createExample(SeckillOrder seckillOrder){
        Example example=new Example(SeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(seckillOrder!=null){
            // 主键
            if(!StringUtils.isEmpty(seckillOrder.getId())){
                    criteria.andEqualTo("id",seckillOrder.getId());
            }
            // 秒杀商品ID
            if(!StringUtils.isEmpty(seckillOrder.getSeckillId())){
                    criteria.andEqualTo("seckillId",seckillOrder.getSeckillId());
            }
            // 支付金额
            if(!StringUtils.isEmpty(seckillOrder.getMoney())){
                    criteria.andEqualTo("money",seckillOrder.getMoney());
            }
            // 用户
            if(!StringUtils.isEmpty(seckillOrder.getUserId())){
                    criteria.andEqualTo("userId",seckillOrder.getUserId());
            }
            // 创建时间
            if(!StringUtils.isEmpty(seckillOrder.getCreateTime())){
                    criteria.andEqualTo("createTime",seckillOrder.getCreateTime());
            }
            // 支付时间
            if(!StringUtils.isEmpty(seckillOrder.getPayTime())){
                    criteria.andEqualTo("payTime",seckillOrder.getPayTime());
            }
            // 状态，0未支付，1已支付
            if(!StringUtils.isEmpty(seckillOrder.getStatus())){
                    criteria.andEqualTo("status",seckillOrder.getStatus());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(seckillOrder.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if(!StringUtils.isEmpty(seckillOrder.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",seckillOrder.getReceiverMobile());
            }
            // 收货人
            if(!StringUtils.isEmpty(seckillOrder.getReceiver())){
                    criteria.andEqualTo("receiver",seckillOrder.getReceiver());
            }
            // 交易流水
            if(!StringUtils.isEmpty(seckillOrder.getTransactionId())){
                    criteria.andEqualTo("transactionId",seckillOrder.getTransactionId());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(Long id){
        seckillOrderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder){
        seckillOrderMapper.updateByPrimaryKey(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder){
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id){
        return  seckillOrderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询SeckillOrder全部数据
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return seckillOrderMapper.selectAll();
    }
}
