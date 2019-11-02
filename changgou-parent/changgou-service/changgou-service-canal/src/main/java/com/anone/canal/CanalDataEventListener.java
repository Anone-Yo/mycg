package com.anone.canal;

import com.alibaba.fastjson.JSON;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.anone.canal.mq.queue.TopicQueue;
import com.anone.canal.mq.send.TopicMessageSender;
import com.anone.content.domain.Content;
import com.xpand.starter.canal.annotation.*;
import entity.Message;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import com.anone.content.feign.ContentFeign;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.List;

//canal 监听类
@CanalEventListener
public class CanalDataEventListener {
    //注入feign
    @Autowired
    private ContentFeign contentFeign;
    //注入redis
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private TopicMessageSender topicMessageSender;


    /***
     * 规格、分类数据修改监听
     * 同步数据到Redis
     * @param eventType
     * @param rowData
     */
    @ListenPoint(destination = "example", schema = "changgou_goods", table = {"tb_spu"}, eventType = {CanalEntry.EventType.UPDATE,CanalEntry.EventType.DELETE})
    public void onEventCustomSpu(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //操作类型
        int number = eventType.getNumber();
        //操作的数据
        String id = getColumn(rowData,"id");
        //封装Message
        Message message = new Message(number, id, TopicQueue.TOPIC_QUEUE_SPU,TopicQueue.TOPIC_EXCHANGE_SPU);
        //发送消息
        topicMessageSender.sendMessage(message);
    }

    /***
     * 获取某个列的值
     * @param rowData
     * @param name
     * @return
     */
    public String getColumn(CanalEntry.RowData rowData , String name){
        //操作后的数据
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            String columnName = column.getName();
            if(columnName.equalsIgnoreCase(name)){
                return column.getValue();
            }
        }
        //操作前的数据
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            String columnName = column.getName();
            if(columnName.equalsIgnoreCase(name)){
                return column.getValue();
            }
        }
        return null;
    }
  /*  //新增监听
    @InsertListenPoint
    public void oneEvenInsert(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        List<CanalEntry.Column> columnsList = rowData.getAfterColumnsList();//获取新增后的数据
        for (CanalEntry.Column column : columnsList) {
            System.out.println("列名："+column.getName());
            System.out.println("新增后的数据："+column.getValue());
        }
    }*/

   /* //删除监听
    @DeleteListenPoint
    public void oneEvenDelete(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //获取删除前的数据
        List<CanalEntry.Column> columnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : columnsList) {
            System.out.println("列名：" + column.getName());
            System.out.println("删除前的数据：" + column.getValue());
        }
    }*/

    //修改监听
    //获取修改前
    //获取修改后
 /*   @UpdateListenPoint
    public void oneEvenUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {*/
       /* //修改前的数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            System.out.println("列名：" + column.getName());
            System.out.println("修改前的数据：" + column.getValue());
        }*/
    /*    //修改后的数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            System.out.println("列名：" + column.getName());
            System.out.println("删除前的数据：" + column.getValue());
        }
    }*/

    //自定义监听
    @ListenPoint(eventType = {CanalEntry.EventType.DELETE, CanalEntry.EventType.UPDATE, CanalEntry.EventType.INSERT},
            schema = {"changgou_content"},
            table = {"tb_content_category", "tb_content"},
            destination = "example")
    public void oneEven(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        //获取到categoryId==>监听的
        String categoryId = getColumn(rowData, "category_id");
        //根据categoryId进行查询
        Result<List<Content>> contentResult = contentFeign.findByCategoryId(Long.valueOf(categoryId));
        List<Content> contents = contentResult.getData();
        //存入redis中
        stringRedisTemplate.boundValueOps("content_"+categoryId).set(JSON.toJSONString(contents));

    }

  /*  public String getColumn(CanalEntry.RowData rowData,String colunmName) {
        //自定义变化前的数据
        List<CanalEntry.Column> beforeColumnsList = rowData.getBeforeColumnsList();
        for (CanalEntry.Column column : beforeColumnsList) {
            if (column.getName().equals(colunmName)) {
                return column.getValue();
            }
        }
        //自定义变化后的数据
        List<CanalEntry.Column> afterColumnsList = rowData.getAfterColumnsList();
        for (CanalEntry.Column column : afterColumnsList) {
            if (column.getName().equals(colunmName)){
                return column.getValue();
            }
        }
        return null;}*/
}
