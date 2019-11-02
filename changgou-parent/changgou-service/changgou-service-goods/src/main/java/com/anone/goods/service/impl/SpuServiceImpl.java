package com.anone.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.anone.goods.dao.BrandMapper;
import com.anone.goods.dao.CategoryMapper;
import com.anone.goods.dao.SkuMapper;
import com.anone.goods.dao.SpuMapper;
import com.anone.goods.domain.*;
import com.anone.goods.service.SpuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;
import entity.*;

import java.util.*;

/****
 * @Author:anone
 * @Description:Spu业务层接口实现类
 *****/
@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SkuMapper skuMapper;

    /***
     * 商品审核
     * @param spuId
     */
    @Override
    public void audit(Long spuId) {
        //根据id获取spu 对象
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //判断spu是否已删除
        if (spu.getIsDelete().equalsIgnoreCase("1")) {
            throw new RuntimeException("当前商品已被删除，不能审核");
        }
        spu.setStatus("1");
        //修改
        spuMapper.updateByPrimaryKeySelective(spu);

        //修改spu
    }

    /**
     * 商品下架
     * @param spuId
     */
    @Override
    public void pull(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsDelete().equalsIgnoreCase("1")) {
            throw new RuntimeException("商品已被删除，不能下架");
        }
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);

    }

    /**
     * 商品上架
     * @param spuId
     */
    @Override
    public void put(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsDelete().equalsIgnoreCase("1")) {
            throw new RuntimeException("商品已被删除，不能上架");
        }
        if (spu.getStatus().equalsIgnoreCase("0")) {
            throw new RuntimeException("商品未审核");
        }
        if (spu.getStatus().equalsIgnoreCase("2")) {
            throw new RuntimeException("商品审核不通过");
        }
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 批量上架
     * @param ids
     * @return
     */
    @Override
    public int putMany(Long[] ids) {
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //更新所有id
        criteria.andIn("id", Arrays.asList(ids));
        //未删除
        criteria.andEqualTo("isDelete","0");
        //已审核
        criteria.andEqualTo("status","1");
        //上架
        Spu spu=new Spu();
        spu.setIsMarketable("1");
        return spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 批量下架
     * @param ids
     * @return
     */
    @Override
    public int pullMany(Long[] ids) {
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        //未删除
        criteria.andEqualTo("isDelete","0");
        //通过审核
        criteria.andEqualTo("status","1");
        //下架
        Spu spu=new Spu();
        spu.setIsMarketable("0");
        return spuMapper.updateByExampleSelective(spu,example);
    }

    /**
     * 逻辑删除
     * @param spuId
     */
    @Override
    public void logicDelete(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        //判断是否上架
        if (spu.getIsMarketable().equalsIgnoreCase("1")) {
            throw  new  RuntimeException("该商品已上架，不能删除");
        }
        //下架才可以进行逻辑删除
        spu.setIsDelete("1");
        //设置未审核
        spu.setStatus("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 还原被删除的商品
     * 判断商品是否被删除
     * @param spuId
     */
    @Override
    public void restore(Long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        if (spu.getIsDelete().equalsIgnoreCase("0")) {
            throw new RuntimeException("商品未删除,无需还原");
        }
        spu.setStatus("0");
        spu.setIsDelete("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 根据spu id查询商品
     */
    @Override
    public Goods findGoods(long spuId) {
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        Sku sku=new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        Goods goods=new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    /**
     * 新增商品(3级分类的信息)
     * 修改=判断spu 是否有id =有则修改 =无则增加
     * sku的增加===修改是先删除再重新新增
     * @param goods
     */
    @Override
    public void addGoods(Goods goods) {
        Spu spu = goods.getSpu();

        //id为空 新增
        if (spu.getId() == null) {
            spu.setId(idWorker.nextId());
            spuMapper.insertSelective(spu);
        } else {
            //id 不为空 修改
            spuMapper.updateByPrimaryKeySelective(spu);
           Sku sku=new Sku();
           sku.setSpuId(spu.getId());
           skuMapper.delete(sku);
        }
        //新增spu
        Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        Date date = new Date();
        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {
            //封装sku 的名称 商品的名称
            String spec = sku.getSpec();
            //防止空指针
            if (StringUtils.isEmpty(spec)) {
                spec="{}";
            }
            Map<String,String> map = JSON.parseObject(spec, Map.class);
            Set<Map.Entry<String, String>> entries = map.entrySet();
            String name=spu.getName();
            for (Map.Entry<String, String> entry : entries) {
                name+=" "+entry.getValue();
            }
            sku.setName(name);
            //封装sku独有id
            sku.setId(idWorker.nextId());
            //封装spuid
            sku.setSpuId(spu.getId());
            //封装 品牌名
            sku.setBrandName(brand.getName());
            //封装 3级分类的id
            sku.setCategoryId(category.getId());//3级分类的id;
            //封装3级分类名
            sku.setCategoryName(category.getName());
            sku.setCreateTime(date);
            sku.setUpdateTime(date);
            //新增sku
            skuMapper.insertSelective(sku);
        }

    }

    /**
     * Spu条件+分页查询
     * @param spu 查询条件
     * @param page 页码
     * @param size 页大小
     * @return 分页结果
     */
    @Override
    public PageInfo<Spu> findPage(Spu spu, int page, int size){
        //分页
        PageHelper.startPage(page,size);
        //搜索条件构建
        Example example = createExample(spu);
        //执行搜索
        return new PageInfo<Spu>(spuMapper.selectByExample(example));
    }

    /**
     * Spu分页查询
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageInfo<Spu> findPage(int page, int size){
        //静态分页
        PageHelper.startPage(page,size);
        //分页查询
        return new PageInfo<Spu>(spuMapper.selectAll());
    }

    /**
     * Spu条件查询
     * @param spu
     * @return
     */
    @Override
    public List<Spu> findList(Spu spu){
        //构建查询条件
        Example example = createExample(spu);
        //根据构建的条件查询数据
        return spuMapper.selectByExample(example);
    }


    /**
     * Spu构建查询对象
     * @param spu
     * @return
     */
    public Example createExample(Spu spu){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(spu!=null){
            // 主键
            if(!StringUtils.isEmpty(spu.getId())){
                    criteria.andEqualTo("id",spu.getId());
            }
            // 货号
            if(!StringUtils.isEmpty(spu.getSn())){
                    criteria.andEqualTo("sn",spu.getSn());
            }
            // SPU名
            if(!StringUtils.isEmpty(spu.getName())){
                    criteria.andLike("name","%"+spu.getName()+"%");
            }
            // 副标题
            if(!StringUtils.isEmpty(spu.getCaption())){
                    criteria.andEqualTo("caption",spu.getCaption());
            }
            // 品牌ID
            if(!StringUtils.isEmpty(spu.getBrandId())){
                    criteria.andEqualTo("brandId",spu.getBrandId());
            }
            // 一级分类
            if(!StringUtils.isEmpty(spu.getCategory1Id())){
                    criteria.andEqualTo("category1Id",spu.getCategory1Id());
            }
            // 二级分类
            if(!StringUtils.isEmpty(spu.getCategory2Id())){
                    criteria.andEqualTo("category2Id",spu.getCategory2Id());
            }
            // 三级分类
            if(!StringUtils.isEmpty(spu.getCategory3Id())){
                    criteria.andEqualTo("category3Id",spu.getCategory3Id());
            }
            // 模板ID
            if(!StringUtils.isEmpty(spu.getTemplateId())){
                    criteria.andEqualTo("templateId",spu.getTemplateId());
            }
            // 运费模板id
            if(!StringUtils.isEmpty(spu.getFreightId())){
                    criteria.andEqualTo("freightId",spu.getFreightId());
            }
            // 图片
            if(!StringUtils.isEmpty(spu.getImage())){
                    criteria.andEqualTo("image",spu.getImage());
            }
            // 图片列表
            if(!StringUtils.isEmpty(spu.getImages())){
                    criteria.andEqualTo("images",spu.getImages());
            }
            // 售后服务
            if(!StringUtils.isEmpty(spu.getSaleService())){
                    criteria.andEqualTo("saleService",spu.getSaleService());
            }
            // 介绍
            if(!StringUtils.isEmpty(spu.getIntroduction())){
                    criteria.andEqualTo("introduction",spu.getIntroduction());
            }
            // 规格列表
            if(!StringUtils.isEmpty(spu.getSpecItems())){
                    criteria.andEqualTo("specItems",spu.getSpecItems());
            }
            // 参数列表
            if(!StringUtils.isEmpty(spu.getParaItems())){
                    criteria.andEqualTo("paraItems",spu.getParaItems());
            }
            // 销量
            if(!StringUtils.isEmpty(spu.getSaleNum())){
                    criteria.andEqualTo("saleNum",spu.getSaleNum());
            }
            // 评论数
            if(!StringUtils.isEmpty(spu.getCommentNum())){
                    criteria.andEqualTo("commentNum",spu.getCommentNum());
            }
            // 是否上架,0已下架，1已上架
            if(!StringUtils.isEmpty(spu.getIsMarketable())){
                    criteria.andEqualTo("isMarketable",spu.getIsMarketable());
            }
            // 是否启用规格
            if(!StringUtils.isEmpty(spu.getIsEnableSpec())){
                    criteria.andEqualTo("isEnableSpec",spu.getIsEnableSpec());
            }
            // 是否删除,0:未删除，1：已删除
            if(!StringUtils.isEmpty(spu.getIsDelete())){
                    criteria.andEqualTo("isDelete",spu.getIsDelete());
            }
            // 审核状态，0：未审核，1：已审核，2：审核不通过
            if(!StringUtils.isEmpty(spu.getStatus())){
                    criteria.andEqualTo("status",spu.getStatus());
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
        //判断是否逻辑删除
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu.getIsDelete().equalsIgnoreCase("0")) {
            throw new RuntimeException("未进行逻辑删除，无法删除商品");
        }
        //物理删除
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 修改Spu
     * @param spu
     */
    @Override
    public void update(Spu spu){
        spuMapper.updateByPrimaryKey(spu);
    }

    /**
     * 增加Spu
     * @param spu
     */
    @Override
    public void add(Spu spu){
        spuMapper.insert(spu);
    }

    /**
     * 根据ID查询Spu
     * @param id
     * @return
     */
    @Override
    public Spu findById(Long id){
        return  spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 查询Spu全部数据
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }
}
