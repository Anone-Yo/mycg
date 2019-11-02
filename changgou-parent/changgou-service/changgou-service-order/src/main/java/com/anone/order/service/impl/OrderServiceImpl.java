package com.anone.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.anone.goods.domain.Sku;
import com.anone.goods.feign.SkuFeign;
import com.anone.order.dao.OrderItemMapper;
import com.anone.order.dao.OrderMapper;
import com.anone.order.domain.Order;
import com.anone.order.domain.OrderItem;
import com.anone.order.service.OrderService;
import com.anone.user.feign.UserFeign;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import entity.IdWorker;
import entity.Result;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/****
 * @Author:anone
 * @Description:Order业务层接口实现类
 *****/
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private SkuFeign skuFeign;
    @Autowired
    private UserFeign userFeign;
    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 修改订单的状态
     */
    @Override
    public void updateStatus(String outtradeno, String payTime, String transactionId) throws Exception {
        //根据订单号查询订单
        Order order = orderMapper.selectByPrimaryKey(outtradeno);
        //创建日期转换对象
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
        Date payTimeInfo = sdf.parse(payTime);
        //修改order
        //支付时间
        order.setPayTime(payTimeInfo);
        //支付状态
        order.setPayStatus("1");
        //微信流水号
        order.setTransactionId(transactionId);
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 逻辑删除 --->修改订单状态
     * @param outtradeno
     */
    @Override
    public void logicDelete(String outtradeno) {
        Order order = orderMapper.selectByPrimaryKey(outtradeno);
        //修改商品删除状态
        order.setIsDelete("1");
        //修改
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     * 增加Order
     * 添加订单
     * @param order
     */
    @GlobalTransactional //开启全局事务控制
    @Override
    public void add(Order order){
        //优化：每次下单，购物车选中的商品数据就会清除
        //场景说明：用户在购物车中提交订单->购物车的商品会删除->订单的商品会增加购物车删除的商品信息
        //获取skuIds
        List<Long> skuIds = order.getSkuIds();
        //从redis中获取用户在购物车中勾选信息（商品详情)
        List<OrderItem> orderItems =new ArrayList<OrderItem>();
        for (Long skuId : skuIds) {
            //先往集合中添加商品数据===>获取订单的详情商品信息
            OrderItem orderItem = (OrderItem) redisTemplate.boundHashOps("cart_" + order.getUsername()).get(skuId);
            orderItems.add(orderItem);
            //删除购物车中指定的商品数据===>购物车的数据删除
            redisTemplate.boundHashOps("cart_"+order.getUsername()).delete(skuId);
        }



        //通过循环遍历商品详情信息，获取订单的总商品数量 和 总金额
        int totalNum=0;
        int totalMoney=0;
        //封装仓库递减的map
        Map<String,Integer> decrMap=new HashMap<String, Integer>();
        for (OrderItem orderItem : orderItems) {
            //进行价格校验
            Sku sku = skuFeign.findById(orderItem.getSkuId()).getData();
            if (sku != null) {
                //从数据库中获取的商品价格（获取商品数量*商品价格）
                Integer skuMoney=sku.getNum() * sku.getPrice();
                //从redis中获取的商品价格
                Integer orderItemMoney = orderItem.getMoney();
                //判断是否出现异价
                if (!sku.equals(orderItemMoney)) {
                    //出现异价则使用数据库中的数据
                    orderItemMoney=skuMoney;
                }

                totalNum += orderItem.getNum();
                totalMoney += orderItemMoney;
            }
            //key：skuid 商品id  value：递减的数量
            decrMap.put(orderItem.getSkuId().toString(),orderItem.getNum());
        }

        //封装订单信息（收货人信息是从前端获取的）
        order.setId(String.valueOf(idWorker.nextId()));//订单id
        order.setTotalNum(totalNum); //总数量
        order.setTotalMoney(totalMoney);//总金额
        order.setPayType("1");//支付类型
        order.setCreateTime(new Date());//创建时间
        order.setUpdateTime(new Date());//更新时间
        order.setSourceType("1");//订单来源
        order.setOrderStatus("0");//订单状态
        order.setPayStatus("0");//支付状态
        order.setConsignStatus("0");//发货状态
        order.setIsDelete("0");//是否删除
        //对订单进行新增
        orderMapper.insertSelective(order);

        //封装商品详情
        for (OrderItem orderItem : orderItems) {
            orderItem.setId(String.valueOf(idWorker.nextId()));
            orderItem.setOrderId(order.getId());
            //新增订单商品详情的信息
            orderItemMapper.insertSelective(orderItem);
        }

        //库存递减
        skuFeign.decrMap(decrMap);

        //增加用户积分
        userFeign.addUserPoint(10);

        //发送订单号给延时队列==>对订单的支付状态进行判断
        //将获取的支付状态信息。发送给延时队列，30分钟之后进行相应处理
        rabbitTemplate.convertAndSend("orderDelayQueue",(Object) order.getId() , new MessagePostProcessor() {
            @Override
            public Message postProcessMessage(Message message) throws AmqpException {
                //设置延时的时间
                message.getMessageProperties().setDelay(10000);//10秒
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.NON_PERSISTENT);//消息持久化
                return message;
            }
        });
    }




    /**
     * Order条件+分页查询
     * @param order 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Order> findPage(Order order, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(order);
        //执行搜索
        return new PageInfo<Order>(orderMapper.selectByExample(example));
    }

    /**
     * Order分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Order> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Order>(orderMapper.selectAll());
    }

    /**
     * Order条件查询
     * @param order
     * @return
     */
    @Override
    public List<Order> findList(Order order){
        //构建查询条件
        Example example = createExample(order);
        //根据构建的条件查询数据
        return orderMapper.selectByExample(example);
    }


    /**
     * Order构建查询对象
     * @param order
     * @return
     */
    public Example createExample(Order order){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(order!=null){
            // 订单id
            if(!StringUtils.isEmpty(order.getId())){
                    criteria.andEqualTo("id",order.getId());
            }
            // 数量合计
            if(!StringUtils.isEmpty(order.getTotalNum())){
                    criteria.andEqualTo("totalNum",order.getTotalNum());
            }
            // 金额合计
            if(!StringUtils.isEmpty(order.getTotalMoney())){
                    criteria.andEqualTo("totalMoney",order.getTotalMoney());
            }
            // 优惠金额
            if(!StringUtils.isEmpty(order.getPreMoney())){
                    criteria.andEqualTo("preMoney",order.getPreMoney());
            }
            // 邮费
            if(!StringUtils.isEmpty(order.getPostFee())){
                    criteria.andEqualTo("postFee",order.getPostFee());
            }
            // 实付金额
            if(!StringUtils.isEmpty(order.getPayMoney())){
                    criteria.andEqualTo("payMoney",order.getPayMoney());
            }
            // 支付类型，1、在线支付、0 货到付款
            if(!StringUtils.isEmpty(order.getPayType())){
                    criteria.andEqualTo("payType",order.getPayType());
            }
            // 订单创建时间
            if(!StringUtils.isEmpty(order.getCreateTime())){
                    criteria.andEqualTo("createTime",order.getCreateTime());
            }
            // 订单更新时间
            if(!StringUtils.isEmpty(order.getUpdateTime())){
                    criteria.andEqualTo("updateTime",order.getUpdateTime());
            }
            // 付款时间
            if(!StringUtils.isEmpty(order.getPayTime())){
                    criteria.andEqualTo("payTime",order.getPayTime());
            }
            // 发货时间
            if(!StringUtils.isEmpty(order.getConsignTime())){
                    criteria.andEqualTo("consignTime",order.getConsignTime());
            }
            // 交易完成时间
            if(!StringUtils.isEmpty(order.getEndTime())){
                    criteria.andEqualTo("endTime",order.getEndTime());
            }
            // 交易关闭时间
            if(!StringUtils.isEmpty(order.getCloseTime())){
                    criteria.andEqualTo("closeTime",order.getCloseTime());
            }
            // 物流名称
            if(!StringUtils.isEmpty(order.getShippingName())){
                    criteria.andEqualTo("shippingName",order.getShippingName());
            }
            // 物流单号
            if(!StringUtils.isEmpty(order.getShippingCode())){
                    criteria.andEqualTo("shippingCode",order.getShippingCode());
            }
            // 用户名称
            if(!StringUtils.isEmpty(order.getUsername())){
                    criteria.andLike("username","%"+order.getUsername()+"%");
            }
            // 买家留言
            if(!StringUtils.isEmpty(order.getBuyerMessage())){
                    criteria.andEqualTo("buyerMessage",order.getBuyerMessage());
            }
            // 是否评价
            if(!StringUtils.isEmpty(order.getBuyerRate())){
                    criteria.andEqualTo("buyerRate",order.getBuyerRate());
            }
            // 收货人
            if(!StringUtils.isEmpty(order.getReceiverContact())){
                    criteria.andEqualTo("receiverContact",order.getReceiverContact());
            }
            // 收货人手机
            if(!StringUtils.isEmpty(order.getReceiverMobile())){
                    criteria.andEqualTo("receiverMobile",order.getReceiverMobile());
            }
            // 收货人地址
            if(!StringUtils.isEmpty(order.getReceiverAddress())){
                    criteria.andEqualTo("receiverAddress",order.getReceiverAddress());
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(!StringUtils.isEmpty(order.getSourceType())){
                    criteria.andEqualTo("sourceType",order.getSourceType());
            }
            // 交易流水号
            if(!StringUtils.isEmpty(order.getTransactionId())){
                    criteria.andEqualTo("transactionId",order.getTransactionId());
            }
            // 订单状态,0:未完成,1:已完成，2：已退货
            if(!StringUtils.isEmpty(order.getOrderStatus())){
                    criteria.andEqualTo("orderStatus",order.getOrderStatus());
            }
            // 支付状态,0:未支付，1：已支付，2：支付失败
            if(!StringUtils.isEmpty(order.getPayStatus())){
                    criteria.andEqualTo("payStatus",order.getPayStatus());
            }
            // 发货状态,0:未发货，1：已发货，2：已收货
            if(!StringUtils.isEmpty(order.getConsignStatus())){
                    criteria.andEqualTo("consignStatus",order.getConsignStatus());
            }
            // 是否删除
            if(!StringUtils.isEmpty(order.getIsDelete())){
                    criteria.andEqualTo("isDelete",order.getIsDelete());
            }
        }
        return example;
    }

    /**
     * 删除
     * @param id
     */
    @Override
    public void delete(String id){
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Order
     * @param order
     */
    @Override
    public void update(Order order){
        orderMapper.updateByPrimaryKey(order);
    }


    /**
     * 根据ID查询Order
     * @param id
     * @return
     */
    @Override
    public Order findById(String id){
        return  orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Order全部数据
     * @return
     */
    @Override
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }
}
