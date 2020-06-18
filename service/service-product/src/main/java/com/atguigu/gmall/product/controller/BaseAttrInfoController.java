package com.atguigu.gmall.product.controller;


import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseAttrValue;
import com.atguigu.gmall.product.service.BaseAttrInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/")
@CrossOrigin
public class BaseAttrInfoController {

    @Autowired
    BaseAttrInfoService baseAttrInfoService;

    @RequestMapping("getAttrValueList/{attrId}")
    public Result<List<BaseAttrValue>> getAttrValueList(@PathVariable Long attrId){

        List<BaseAttrValue> baseAttrValues = baseAttrInfoService.getAttrValueList(attrId);

        return Result.ok(baseAttrValues);
    }



    @RequestMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){

        baseAttrInfoService.saveAttrInfo(baseAttrInfo);

        return Result.ok();
    }


   @RequestMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
   public Result attrInfoList(@PathVariable String category1Id, @PathVariable String category2Id, @PathVariable String category3Id){

       List<BaseAttrInfo> baseAttrInfos =  baseAttrInfoService.attrInfoList(category1Id,category2Id,category3Id);

       return Result.ok(baseAttrInfos);
   }

}
