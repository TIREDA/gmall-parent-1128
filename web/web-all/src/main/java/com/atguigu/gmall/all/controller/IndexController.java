package com.atguigu.gmall.all.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class IndexController {

    @Autowired
    ProductFeignClient productFeignClient;

    @RequestMapping({"/","index"})
    public String index(ModelMap modelMap){
        Result result = productFeignClient.getBaseCategoryList();

        modelMap.put("list",result.getData());
        return "index/index";
    }

}
