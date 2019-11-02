package com.anone.search.controller;

import com.anone.search.domain.SkuInfo;
import com.anone.search.feign.SkuFeign;
import entity.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@Controller
@RequestMapping("/search")
public class SkuController {

    @Autowired
    private SkuFeign skuFeign;

    /**
     * 搜索
     * @param searchMap
     * @param model
     * @return
     */
    @RequestMapping("/list")
    public String search(@RequestParam(required = false) Map<String, String> searchMap, Model model) throws Exception{
        //替换特殊符号
        handlerSearchMap(searchMap);

        //查询所有商品的数据
        Map<String, Object> resultMap = skuFeign.searchData(searchMap);
        //将查询的结果集对象返回到页面
        model.addAttribute("result",resultMap);
        //回显数据
        model.addAttribute("searchMap",searchMap);
        //获取请求地址
        String[] urls=url(searchMap);
        model.addAttribute("url",urls[0]);
        model.addAttribute("sorturl",urls[1]);
        //封装分页条件
        Page<SkuInfo> page=new Page<SkuInfo>(
               Integer.parseInt( resultMap.get("total").toString()),
                //注意：当前是从0开始算
                Integer.parseInt( resultMap.get("pageNumber").toString()+1),
                Integer.parseInt( resultMap.get("pageSize").toString())
        );
        model.addAttribute("pageInfo",page);
        return "search";
    }
    //替换特殊符号
    public void handlerSearchMap(Map<String, String> searchMap) {
        if (searchMap != null) {
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                if (entry.getKey().startsWith("spec_")) {
                    entry.setValue(entry.getValue().replace("+","%2B"));
                }
            }
        }
    }

    /**
     * url组装和处理
     * @param searchMap
     * @return
     */
    public String[] url(Map<String, String> searchMap) {
        //指定url 地址
        String url="/search/list";
        String sorturl="/search/list";
        //判断map
        if (searchMap != null && searchMap.size() > 0) {
            //拼接
            url +="?";
            for (Map.Entry<String, String> entry : searchMap.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                //跳过分页 的拼接
                if (key.equalsIgnoreCase("pageNum") || key.equalsIgnoreCase("pageSize")) {
                    continue;
                }

                //拼接
                url +=key+"="+value+"&";
                //sorturl 不进行拼接条件，跳过sort的条件
                if (key.equalsIgnoreCase("sortField") || key.equalsIgnoreCase("sortRule")) {
                    continue;
                }
                //这里没有拼接sort条件
                sorturl+=key+"="+value+"&";
            }
            //截取
          url=  url.substring(0,url.length()-1);
            sorturl =  sorturl.substring(0,sorturl.length()-1);
        }
        return new  String[]{url,sorturl};
    }
}
