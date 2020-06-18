package com.atguigu.gmall.list.serivce.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.list.repository.GoodsRepository;
import com.atguigu.gmall.list.serivce.SearchService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.commons.lang3.StringUtils;

import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class SearchServiceImpl implements SearchService {


   @Autowired
    ElasticsearchRepository elasticsearchRepository;

    @Autowired
    GoodsRepository goodsRepository;

    @Autowired
    ProductFeignClient productFeignClient;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    public static void main(String[] args) {
        SearchParam searchParam = new SearchParam();
        searchParam.setKeyword("关键字");
        searchParam.setCategory3Id(3l);
        searchParam.setCategory2Id(2l);
        searchParam.setCategory1Id(1l);

        String[] searchAttrs = {"1:1G:111","2:2G:222"};

        searchParam.setProps(searchAttrs);

        buildQueryDsl(searchParam);
    }


    @Override
    public SearchResponseVo list(SearchParam searchParam) {

        // 调用检索接口
        SearchRequest searchRequest = this.buildQueryDsl(searchParam);// 调用封装dsl语句的方法
        SearchResponse searchResponse = null;
        try {
            searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);//调用es
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 解析返回结果
        SearchResponseVo responseVo = this.parseSearchResult(searchResponse);


        return responseVo;
    }


    private SearchResponseVo parseSearchResult(SearchResponse searchResponse) {

        SearchResponseVo searchResponseVo = new SearchResponseVo();
        SearchHits allHits = searchResponse.getHits();
        SearchHit[] sourceHits = allHits.getHits();

        List<Goods> goods = new ArrayList<>();
        for (SearchHit sourceHit : sourceHits) {

            String sourceAsString = sourceHit.getSourceAsString();

            Goods good = JSON.parseObject(sourceAsString, Goods.class);
            goods.add(good);
        }

        searchResponseVo.setGoodsList(goods);
        return searchResponseVo;
    }

    // 调用封装dsl语句的方法
    private static SearchRequest buildQueryDsl(SearchParam searchParam) {


        //把查询的数据封装起来
        /*
        要封装的数据：品牌名， 1-3级id， 平台属性， 分页数据
         */
        // 分页
        Integer pageNo = searchParam.getPageNo();
        Integer pageSize = searchParam.getPageSize();
        // 品牌
        String trademark = searchParam.getTrademark();
        //平台属性id
        String[] props = searchParam.getProps();
        // 排序
        String order = searchParam.getOrder();
        //关键字
        String keyword = searchParam.getKeyword();
        // 三级id
        Long category1Id = searchParam.getCategory1Id();
        Long category2Id = searchParam.getCategory2Id();
        Long category3Id = searchParam.getCategory3Id();

        //总{}语句
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //bool复合查询
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();

        if (StringUtils.isNoneBlank(keyword)) {
            boolQueryBuilder.must(new MatchQueryBuilder("title",keyword));
        }
        if (null != category1Id) {
            boolQueryBuilder.filter(new TermQueryBuilder("category1Id",category1Id));
        }
        if (null != category2Id) {
            boolQueryBuilder.filter(new TermQueryBuilder("category2Id",category2Id));
        }
        if (null != category3Id) {
            boolQueryBuilder.filter(new TermQueryBuilder("category3Id",category3Id));
        }

        //分页
        //第几页
        searchSourceBuilder.from(0);
        //有多少条数据
        searchSourceBuilder.size(60);

        //属性条件
        if (null != props && props.length > 0){
            for (String prop : props) {
                // prop = 23:4G:运行内存
                //split表达式：用于放特殊符号
                String[] split = prop.split(":");
                String attrId = split[0];//属性id
                String attrValue = split[1];//属性值
                String attrName = split[2];//属性名

                /// 最内层bool查询，匹配查询
                BoolQueryBuilder subQueryForProps = new BoolQueryBuilder();
                subQueryForProps.must(new MatchQueryBuilder("attrValue",attrValue));
                subQueryForProps.must(new MatchQueryBuilder("attrId",attrId));

                // 第二层嵌套匹配条件bool查询
                BoolQueryBuilder boolQueryForProps = new BoolQueryBuilder();
                // 平台属性集合对象,嵌套内容，不参与评分
                boolQueryForProps.must(new NestedQueryBuilder("attrs",subQueryForProps, ScoreMode.None));

                // 封装进外层的bool，
                boolQueryBuilder.filter(boolQueryForProps);

            }
        }

        //商标聚合
        TermsAggregationBuilder termsAggregationBuilderTradeMark = AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNmeAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl"));
        //属性聚合

        // 将复合搜素的条件放入query
        searchSourceBuilder.query(boolQueryBuilder);
        searchSourceBuilder.aggregation(termsAggregationBuilderTradeMark);

        // 设置搜索的库和表封装搜索请求对象
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);
        // 打印dsl语句
        System.out.println(searchSourceBuilder.toString());

        return searchRequest;
    }


    @Override
    public void upperGoods(Long skuId) {
        Goods goods = new Goods();

        //查询goods信息，skuInfo，category，trademark，baseAttrInfo


        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        if (skuId!=null){
            // 封装商品数据
            goods.setDefaultImg(skuInfo.getSkuDefaultImg());//图片
            goods.setPrice(skuInfo.getPrice().doubleValue());//价格
            goods.setId(skuInfo.getId());// 商品Id
            goods.setTitle(skuInfo.getSkuName());// title = skuName商品名称
            goods.setCreateTime(new Date());// 创建时间

            // 查询商标数据
            BaseTrademark baseTrademark =productFeignClient.getTrademarkByTmId(skuInfo.getTmId());

            // 将查询出来的基础信息封装到商品goods中
            if (baseTrademark != null){
                goods.setTmId(skuInfo.getTmId());
                goods.setTitle(baseTrademark.getTmName());
                goods.setTmLogoUrl(baseTrademark.getLogoUrl());

            }
            // 查询分类数据
            BaseCategoryView baseCategoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            // 查询分类
            if (baseCategoryView != null){
                goods.setCategory1Id(baseCategoryView.getCategory1Id());
                goods.setCategory1Name(baseCategoryView.getCategory1Name());
                goods.setCategory2Id(baseCategoryView.getCategory2Id());
                goods.setCategory2Name(baseCategoryView.getCategory2Name());
                goods.setCategory3Id(baseCategoryView.getCategory3Id());
                goods.setCategory3Name(baseCategoryView.getCategory3Name());
            }
            // 查询平台属性
            List<SearchAttr> searchAttrs = productFeignClient.getAttrList(skuId);

            goods.setAttrs(searchAttrs);// 平台属性集合对象

            goodsRepository.save(goods);
        }

    }

    @Override
    public void lowerGoods(Long skuId) {
        goodsRepository.deleteById(skuId);
    }


}
