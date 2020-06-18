package com.atguigu.gmall.product.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(value ="service-product")
public interface ProductFeignClient {

    @RequestMapping("/api/product/inner/getSkuInfo/{skuId}")
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    @RequestMapping("/api/product/inner/getSkuPrice/{skuId}")
    public BigDecimal getSkuPrice(@PathVariable("skuId") Long skuId);

    @RequestMapping("/api/product/inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id);

    @RequestMapping("/api/product/inner/getSpuSaleAttrListCheckBySku/{skuId}/{spuId}")
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(@PathVariable("skuId") Long skuId, @PathVariable("spuId") Long spuId);

    @RequestMapping("/api/product/inner/getSkuValueIdsMap/{spuId}")
    Map getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    @RequestMapping("/api/product/getBaseCategoryList")
    Result getBaseCategoryList();

//    @RequestMapping("/api/product/inner/getTrademarkByTmId/{tmId}")
//    BaseTrademark getTrademarkByTmId(@PathVariable("tmId") Long tmId);
//
//    @RequestMapping("/api/product/inner/getAttrList/{skuId}")
//    List<SearchAttr> getAttrList(@PathVariable("skuId") Long skuId);
    @RequestMapping("/api/product/inner/getTrademarkByTmId/{tmId}")
    BaseTrademark getTrademarkByTmId(@PathVariable("tmId") Long tmId);

    @RequestMapping("/api/product/inner/getAttrList/{skuId}")
    List<SearchAttr> getAttrList(@PathVariable("skuId") Long skuId);
}
