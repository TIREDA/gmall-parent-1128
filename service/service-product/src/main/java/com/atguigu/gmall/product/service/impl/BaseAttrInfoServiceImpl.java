package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.product.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BaseAttrInfoServiceImpl implements BaseAttrInfoService {

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> attrInfoList(String category1Id, String category2Id, String category3Id) {

        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectBaseAttrInfoList(Long.parseLong(category1Id), Long.parseLong(category2Id), Long.parseLong(category3Id));

        return baseAttrInfos;
    }

    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        Long attrId = baseAttrInfo.getId();

        if(attrId !=null){
            // 修改属性
            baseAttrInfoMapper.updateById(baseAttrInfo);
            QueryWrapper queryWrapper = new QueryWrapper<BaseAttrValue>();
            queryWrapper.eq("attr_id",baseAttrInfo.getId());
            baseAttrValueMapper.delete(queryWrapper);
        }else{
            // 添加属性
            baseAttrInfoMapper.insert(baseAttrInfo);
            attrId = baseAttrInfo.getId();
        }

        // 不管添加还是修改，最后属性值表都需要重新载入数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(attrId);
            baseAttrValueMapper.insert(baseAttrValue);
        }



    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long attrId) {

        QueryWrapper<BaseAttrValue> queryWrapper  =  new QueryWrapper<BaseAttrValue>();

        queryWrapper.eq("attr_id",attrId);

        List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(queryWrapper);

        return baseAttrValues;
    }

    @Override
    public List<SearchAttr> getAttrList(Long skuId) {
        List<SearchAttr> searchAttrs = baseAttrInfoMapper.selectAttrList(skuId);
        return searchAttrs;
    }
}
