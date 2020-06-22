package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.serivce.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/list")
public class ListApiController {

    @Autowired
    ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    SearchService searchService;

    @RequestMapping("inner/incrHotScore/{skuId}")
    void incrHotScore(@PathVariable("skuId") Long skuId){
        searchService.incrHotScore(skuId);
    }

    /**
     *
     * @return
     */
    @GetMapping("inner/createIndex")
    public Result createIndex() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
        return Result.ok();
    }

    @RequestMapping("inner/upperGoods/{skuId}")
    public Result upperGoods(@PathVariable("skuId") Long skuId) {
        // 调用listService的商品上架功能
        searchService.upperGoods(skuId);
        return Result.ok();
    }

    @RequestMapping("inner/lowerGoods/{skuId}")
    public Result lowerGoods(@PathVariable("skuId") Long skuId) {
        // 调用listService的商品删除功能
        searchService.lowerGoods(skuId);
        return Result.ok();
    }

    @RequestMapping("list")
    Result<Map> list(@RequestBody SearchParam searchParam){

        SearchResponseVo searchResponseVo = searchService.list(searchParam);

        Map<String,Object> map = new HashMap<>();
        map.put("goodsList",searchResponseVo.getGoodsList());//详情
        map.put("attrsList",searchResponseVo.getAttrsList());//网络制式
        map.put("trademarkList",searchResponseVo.getTrademarkList());//品牌
        return Result.ok(map);
    }
}
