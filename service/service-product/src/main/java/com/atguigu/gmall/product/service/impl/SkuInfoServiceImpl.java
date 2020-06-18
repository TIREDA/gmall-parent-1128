package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.cache.GmallCache;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.list.client.ListFileClient;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.SkuInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SkuInfoServiceImpl implements SkuInfoService {

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    SkuImageMapper skuImageMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Autowired
    ListFileClient listFileClient;


    @Override
    public IPage<SkuInfo> selectPage(Page<SkuInfo> pageParam) {
        QueryWrapper<SkuInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        IPage<SkuInfo> page = skuInfoMapper.selectPage(pageParam, queryWrapper);
        return page;
    }


    @Override
    public List<SpuImage> getSpuImageList(Long spuId) {
        QueryWrapper<SpuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(queryWrapper);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(spuId);
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        skuInfoMapper.insert(skuInfo);// 返回保存得sku主键

        Long skuId = skuInfo.getId();

        // 保存销售属性中间表
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuId);
            skuAttrValueMapper.insert(skuAttrValue);
        }


        // 保存平台属性平台属性中间表
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuId);
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        }


        // 保存sku图片表
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuId);
            skuImageMapper.insert(skuImage);
        }

    }

    @Override
    public void onSale(Long skuId) {

        SkuInfo skuInfo = new SkuInfo();

        skuInfo.setId(skuId);
        skuInfo.setIsSale(1);

        //调用service-uitl中的方法获得商品信息存入es中
        listFileClient.upperGoods(skuId);


        skuInfoMapper.updateById(skuInfo);

    }

    @Override
    public void cancelSale(Long skuId) {

        SkuInfo skuInfo = new SkuInfo();

        skuInfo.setId(skuId);
        skuInfo.setIsSale(0);

        //调用service-uitl中的方法删除商品信息
        listFileClient.lowerGoods(skuId);
//        listFileClient.upperGoods(null);
        skuInfoMapper.updateById(skuInfo);

    }


    /***
     * 使用aop缓存注解后
     * @param skuId
     * @return
     */
    @GmallCache
    @Override
    public SkuInfo getSkuInfo(Long skuId) {

        SkuInfo skuInfoDB = getSkuInfoDB(skuId);

        return skuInfoDB;
    }


    /***
     * 使用aop缓存注解前
     * @param skuId
     * @return
     */
    public SkuInfo getSkuInfoBak(Long skuId) {
        SkuInfo skuInfo = null;
        String skuRedisKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
        //查询缓存 ,缓存中的商品详情key
        String skuInfoStr = (String)redisTemplate.opsForValue().get(skuRedisKey);
        if(StringUtils.isNotBlank(skuInfoStr)){
            skuInfo = JSON.parseObject(skuInfoStr,SkuInfo.class);
        }
        if (skuInfo == null) {
            // 用来删除分布式锁的uuid
            String uuid = UUID.randomUUID().toString();
            // 分布式锁的key，sku:15:lock
            Boolean OK = redisTemplate.opsForValue().setIfAbsent("sku:" + skuId + ":lock", uuid, 10, TimeUnit.SECONDS);
            if (OK) {
                // 查询db
                skuInfo = getSkuInfoDB(skuId);
                if(skuInfo==null){
                    SkuInfo skuInfo1 = new SkuInfo();
                    redisTemplate.opsForValue().set(skuRedisKey, JSON.toJSONString(skuInfo1),60*60,TimeUnit.SECONDS);//将空的sku对象存入缓存
                    return skuInfo1;
                }
                redisTemplate.opsForValue().set(skuRedisKey, JSON.toJSONString(skuInfo));//缓存中的商品详情key

                // 使用lua脚本删除分布式锁 // lua，在get到key后，根据key的具体值删除key
                DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();
                //luaScript.setResultType(Long.class);
                luaScript.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
                redisTemplate.execute(luaScript, Arrays.asList("sku:" + skuId + ":lock"), uuid);
                return skuInfo;
            }else {
                // 没有获取到分布式锁，1秒后开始自选
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuInfo(skuId);
            }
        }
        return skuInfo;
    }

    /***
     * 通过db查询sku
     * @param skuId
     * @return
     */
    private SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        QueryWrapper<SkuImage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImages = skuImageMapper.selectList(queryWrapper);

        skuInfo.setSkuImageList(skuImages);

        return skuInfo;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        BigDecimal price = skuInfo.getPrice();

        return price;
    }

    @Override
    public Map getSkuValueIdsMap(Long spuId) {

        List<Map> list = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);

        // 返回结果的map
        Map<String, String> map = new HashMap<>();

        // 处理将返回结果封装给map
        // 循环遍历
        if (list != null && list.size() > 0) {
            for (Map<String, String> skuMap : list) {// 数据map
                // key = 125|123 ,value = 37
                map.put(skuMap.get("value_ids"), skuMap.get("sku_id"));
            }
        }

        return map;
    }


}
