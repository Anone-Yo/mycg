package com.anone.seckill.timer;

import com.anone.seckill.dao.SeckillGoodsMapper;
import com.anone.seckill.domain.SeckillGoods;
import entity.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 定时任务==>将秒杀商品存入redis中
 */
@Component
public class SeckillGoodsPushTask {

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     *将秒杀商品定时存入redis中
     */
    @Scheduled(cron = "0/5 * * * * ?") //设置定时的时间
    public void loadGoodsPushRedis() {
        //求时间菜单
        List<Date> dateMenus = DateUtil.getDateMenus();
        //循环查询每个时间区间的秒杀商品
        for (Date dateMenu : dateMenus) {
            String timespace="seckillGoods_"+DateUtil.data2str(dateMenu,"yyyyMMddHH");
            /**
             * 使用example对象 设置条件 进行查询
             * 1.审核状态是通过的
             * 2.秒杀商品的存储大于0
             * 3.时间菜单的开始时间<=start_time end_time<开始时间+2小时
             */
            //查询秒杀商品 进行条件封装
            Example example=new Example(SeckillGoods.class);
            Example.Criteria criteria = example.createCriteria();

            //审核状态，通过 为1
            criteria.andEqualTo("status","1");
            //秒杀商品的库存大于0
            criteria.andGreaterThan("stockCount","0");
            //时间菜单的开始时间<=start_time
            criteria.andGreaterThanOrEqualTo("startTime",dateMenu);
            //endTime<时间菜单的开始时间+2
            criteria.andLessThan("endTime",DateUtil.addDateHour(dateMenu,2));
            //排除已经存入到redis中的seckillGoods
            //获取命名空间下所有商品的id
            //每次查询排除已经存在的id的数据
            Set keys = redisTemplate.boundHashOps(timespace).keys();
            if (keys != null && keys.size() > 0) {
                criteria.andNotIn("id",keys);
            }
            //进行查询
            List<SeckillGoods> seckillGoods = seckillGoodsMapper.selectByExample(example);

            //将秒杀商品存入到redis中(使用hash类型存入)
            //timespace:命名空间 格式：seckillGoods_20190901210
            for (SeckillGoods seckillGood : seckillGoods) {
               //将查询出来的秒杀商品存入到Redis 中
                redisTemplate.boundHashOps(timespace).put(seckillGood.getId(),seckillGood);
                //防止超卖现象
                Long[] ids = getIds(seckillGood.getStockCount(), seckillGood.getId());
                //将每个秒杀商品单独存入一个队列redis中
                redisTemplate.boundListOps("SeckillGoodsCountList_"+seckillGood.getId()).leftPushAll(ids);
            }
        }
    }

    //获取每个秒杀商品的ids集合
    public Long[] getIds(Integer num,Long id) {
       Long[] ids=new Long[num];
        for (int i = 0; i <ids.length ; i++) {
            ids[i]=id;
        }
        return ids;
    }

}
