package com.atguigu.gmall.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.list.client.ListFileClient;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    ThreadPoolExecutor threadPoolExecutor;

    @Autowired
    ListFileClient listFileClient;


    @Override
    public Map<String, Object> getSkuById(Long skuId) {


        // 单线程跑
        // Map<String, Object> skuByIdSingle = getSkuByIdSingle(skuId);

        // 多线程跑
        Map<String, Object> skuByIdThread = getSkuByIdThread(skuId);

        return skuByIdThread;
    }

    /***
     * 单线程方法
     * @param skuId
     * @return
     */
    private Map<String, Object> getSkuByIdSingle(Long skuId) {

        long currentTimeMilliStart = System.currentTimeMillis();
        System.out.println("单线程开始时间："+currentTimeMilliStart);

        Map<String, Object> map = new HashMap<>();

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);

        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());

        // 价格信息
        BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);


        // 页面销售属性列表信息
        List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());

        // 页面销售属性map
        Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());

        map.put("price", skuPrice);
        map.put("categoryView", categoryView);
        map.put("skuInfo", skuInfo);// 将skuInfo信息放入结果集
        map.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        // 放入销售属性对应skuId的map
        map.put("valuesSkuJson", JSON.toJSONString(skuValueIdsMap));

        long currentTimeMillisEnd = System.currentTimeMillis();
        System.out.println("单线程结束时间："+currentTimeMillisEnd);

        Long time = currentTimeMillisEnd - currentTimeMilliStart;

        System.out.println("单线程用时："+time+ "毫秒执行时间");
        return map;
    }


    /***
     * 多线程方法
     */
    private Map<String, Object> getSkuByIdThread(Long skuId) {
        long currentTimeMilliStart = System.currentTimeMillis();
        System.out.println("多线程开始时间："+currentTimeMilliStart);

        Map<String, Object> map = new HashMap<>();

        // 查询skuInfo信息的独立线程操作
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            // sku信息
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            map.put("skuInfo", skuInfo);// 将skuInfo信息放入结果集
            return skuInfo;
        },threadPoolExecutor);

        // 分类信息线程操作需要依赖于skuInfo信息，并且不需要返回结果
        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            map.put("categoryView", categoryView);
        },threadPoolExecutor);

        // 价格信息
        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal skuPrice = productFeignClient.getSkuPrice(skuId);
            map.put("price", skuPrice);
        },threadPoolExecutor);

        // 页面销售属性列表信息
        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrListCheckBySku = productFeignClient.getSpuSaleAttrListCheckBySku(skuId, skuInfo.getSpuId());
            map.put("spuSaleAttrList", spuSaleAttrListCheckBySku);
        },threadPoolExecutor);


        // 页面销售属性map
        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map skuValueIdsMap = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            map.put("valuesSkuJson", skuValueIdsMap);
        },threadPoolExecutor);

        // 为了防止主线程在数据结果封装完成之前就返回map，用allOf控制，保证所有异步线程全部完成任务
        CompletableFuture.allOf(skuInfoCompletableFuture, categoryViewCompletableFuture, priceCompletableFuture, spuSaleAttrListCompletableFuture, valuesSkuJsonCompletableFuture).join();

        // 商品被访问，为搜索增加热度值
        listFileClient.incrHotScore(skuId);

        long currentTimeMillisEnd = System.currentTimeMillis();
        System.out.println("多线程结束时间："+currentTimeMillisEnd);

        Long time = currentTimeMillisEnd - currentTimeMilliStart;

        System.out.println("多线程用时："+time + "毫秒执行时间");

        return map;
    }
}
