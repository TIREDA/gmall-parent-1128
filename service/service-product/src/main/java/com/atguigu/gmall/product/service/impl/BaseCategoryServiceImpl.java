package com.atguigu.gmall.product.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.product.BaseCategory1;
import com.atguigu.gmall.model.product.BaseCategory2;
import com.atguigu.gmall.model.product.BaseCategory3;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.mapper.BaseCategory1Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory2Mapper;
import com.atguigu.gmall.product.mapper.BaseCategory3Mapper;
import com.atguigu.gmall.product.mapper.BaseCategoryViewMapper;
import com.atguigu.gmall.product.service.BaseCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
public class BaseCategoryServiceImpl implements BaseCategoryService {

    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;

    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;

    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;

    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;

    @Override
    public List<BaseCategory1> getCategory1() {

        List<BaseCategory1> baseCategory1s = baseCategory1Mapper.selectList(null);

        return baseCategory1s;
    }

    @Override
    public List<BaseCategory2> getCategory2(String category1Id) {

        QueryWrapper wrapper = new QueryWrapper<BaseCategory2>();
        wrapper.eq("category1_id",category1Id);
        List<BaseCategory2> baseCategory2s = baseCategory2Mapper.selectList(wrapper);

        return baseCategory2s;
    }

    @Override
    public List<BaseCategory3> getCategory3(String category2Id) {
        QueryWrapper wrapper = new QueryWrapper<BaseCategory3>();
        wrapper.eq("category2_id",category2Id);
        List<BaseCategory3> baseCategory3s = baseCategory3Mapper.selectList(wrapper);

        return baseCategory3s;
    }

    @Override
    public BaseCategoryView getCategoryViewByCategory3Id(Long category3Id) {

        QueryWrapper<BaseCategoryView> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("category3_id",category3Id);
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectOne(queryWrapper);

        return baseCategoryView;
    }

    @Override
    public List<JSONObject> getBaseCategoryList() {


        List<BaseCategoryView> baseCategoryViews = baseCategoryViewMapper.selectList(null);
        //查询并且封装页面上需要的json集合
        Map<Long, List<BaseCategoryView>> category1Map  = baseCategoryViews.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        Set<Map.Entry<Long, List<BaseCategoryView>>> entries = category1Map.entrySet();

        int index = 0;
        //封装一级分类的集合
        List<JSONObject> list = new ArrayList<>();
        for (Map.Entry<Long, List<BaseCategoryView>> entry : entries) {
            Long category1Id = entry.getKey();//一级分类id
            List<BaseCategoryView> category2List1 = entry.getValue();

            JSONObject category1 = new JSONObject();

            category1.put("index",index);
            category1.put("categoryId",category1Id);
            category1.put("categoryName",category2List1.get(0).getCategory1Name());

            //封装二级分类的集合
            List<JSONObject> category2Child = new ArrayList<>();//二级分类集合
            Map<Long, List<BaseCategoryView>> category2Map = category2List1.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
            Set<Map.Entry<Long, List<BaseCategoryView>>> entries2 = category2Map.entrySet();

            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : entries2) {
                Long category2Id = entry2.getKey();
                List<BaseCategoryView> category3List1 = entry2.getValue();//三级分类集合
                JSONObject category2 = new JSONObject();
                category2.put("index", index);
                category2.put("categoryId", category2Id);
                category2.put("categoryName", category3List1.get(0).getCategory2Name());
                category2Child.add(category2);

                //封装三级分类的集合
                List<JSONObject> category3Child = new ArrayList<>();//三级分类集合
                // 循环三级分类数据
                category3List1.stream().forEach(category3View -> {
                    JSONObject category3 = new JSONObject();
                    category3.put("categoryId", category3View.getCategory3Id());
                    category3.put("categoryName", category3View.getCategory3Name());

                    category3Child.add(category3);
                });

                // 将三级数据放入二级里面
                category2.put("categoryChild", category3Child);
            }

            category1.put("categoryChild",category2Child);//二级分类集合
            list.add(category1);
            index++;
        }


        return list;
    }
}
