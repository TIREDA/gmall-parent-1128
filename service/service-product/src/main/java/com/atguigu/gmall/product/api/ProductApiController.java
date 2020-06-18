package com.atguigu.gmall.product.api;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
public class ProductApiController {

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    BaseCategoryService baseCategoryService;

    @Autowired
    SpuInfoService spuInfoService;

    @Autowired
    BaseTrademarkService baseTrademarkService;

    @Autowired
    BaseAttrInfoService baseAttrInfoService;

    @RequestMapping("inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId) {
        SkuInfo skuInfo = skuInfoService.getSkuInfo(skuId);
        return skuInfo;
    }

    @RequestMapping("inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId) {
        return skuInfoService.getSkuPrice(skuId);
    }

    @RequestMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryService.getCategoryViewByCategory3Id(category3Id);
        return baseCategoryView;
    }

    @RequestMapping("inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId) {
        List<SpuSaleAttr> spuSaleAttrs = spuInfoService.getSpuSaleAttrListCheckBySku(skuId,spuId);
        return spuSaleAttrs;
    }

    @RequestMapping("inner/getSkuValueIdsMap/{spuId}")
    Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
        Map map = skuInfoService.getSkuValueIdsMap(spuId);
        return map;
    }

    @RequestMapping("inner/getTrademarkByTmId/{tmId}")
    BaseTrademark getTrademarkByTmId(@PathVariable("tmId") Long tmId){
        BaseTrademark baseTrademark = baseTrademarkService.getTrademarkByTmId(tmId);
        return baseTrademark;
    }

    @RequestMapping("inner/getAttrList/{skuId}")
    List<SearchAttr> getAttrList(@PathVariable("skuId") Long skuId){
        List<SearchAttr> searchAttrs = baseAttrInfoService.getAttrList(skuId);
        return searchAttrs;
    }

    @RequestMapping("getBaseCategoryList")
    Result getBaseCategoryList(){
        List<JSONObject> list = baseCategoryService.getBaseCategoryList();

        return Result.ok(list);
    }

}
