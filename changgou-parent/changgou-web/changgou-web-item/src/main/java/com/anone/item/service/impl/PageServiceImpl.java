package com.anone.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.anone.goods.domain.Category;
import com.anone.goods.domain.Sku;
import com.anone.goods.domain.Spu;
import com.anone.goods.feign.SkuFeign;
import com.anone.goods.feign.SpuFeign;
import com.anone.goods.feign.CategoryFeign;
import com.anone.item.service.PageService;
import entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {
    //注入templateEngine对象来生成静态页面
   @Autowired
   private SpuFeign spuFeign;
   @Autowired
   private SkuFeign skuFeign;
   @Autowired
   private CategoryFeign categoryFeign;
    @Autowired
    private TemplateEngine templateEngine;
   /* //生成静态文件路径
    @Value("${pagepath}")
    private String pagepath;
*/
    @Override
    public void createPageHtml(Long spuId) {
        //创建容器对象
        Context context=new Context();
        //构建查询
        Map<String, Object> dataMap = buildDataModel(spuId);
        context.setVariables(dataMap);
        //获取类路径===>获取到Resource目录下的items文件
        String pagepath = PageServiceImpl.class.getResource("/").getPath() + "/items";

        //准备文件
        File file=new File(pagepath);
        //判断文件是否存在
        if (!file.exists()) {
            file.mkdirs();
        }
        //创建writer对象
        try {
            //将静态页面生成到指定的路径
            FileWriter fileWriter=new FileWriter(pagepath+"/"+spuId+".html");

            //生成静态页面
            //process
            //参数1：模板 以classpath:/tempaltes/xxx.html 这里已经拼接好
            //参数2：容器===>存入数据
            //参数3：文件输出流，将文件写到指定路径，生成指定的文件
            templateEngine.process("item",context,fileWriter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 构建查询的数据
     * @param spuId
     * @return
     */
    public Map<String, Object> buildDataModel(Long spuId) {
        //查询spu
        Spu spu = spuFeign.findById(spuId).getData();
        //获取三个分类的id
        Integer category1Id = spu.getCategory1Id();
        Integer category2Id = spu.getCategory2Id();
        Integer category3Id = spu.getCategory3Id();
        //查询三个分类
        Result<Category> category1Result = categoryFeign.findById(category1Id);
        Result<Category> category2Result = categoryFeign.findById(category2Id);
        Result<Category> category3Result = categoryFeign.findById(category3Id);
        //查询skuList
        //查询sku->封装spuid ->查询与spuid有关的
        Sku sku=new Sku();
        sku.setSpuId(spuId);
        Result<List<Sku>> skuListResult = skuFeign.findList(sku);
        //处理图片
        String[] images = spu.getImages().split(",");
        //设置规格==》获取规格集合
        String specItems = spu.getSpecItems();
        Map specMap = JSON.parseObject(specItems, Map.class);
        //封装到map中
        Map<String,Object> dataMap= new HashMap<String, Object>();
        dataMap.put("spu",spu);
        dataMap.put("category1",category1Result.getData());
        dataMap.put("category2",category2Result.getData());
        dataMap.put("category3",category3Result.getData());
        dataMap.put("imagesList",images);
        dataMap.put("specificationList",skuListResult.getData());
        return dataMap;
    }
}
