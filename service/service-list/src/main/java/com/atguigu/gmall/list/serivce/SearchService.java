package com.atguigu.gmall.list.serivce;

import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface SearchService{

    void upperGoods(Long skuId);

    void lowerGoods(Long skuId);

    SearchResponseVo list(SearchParam searchParam);

    void incrHotScore(Long skuId);
}
