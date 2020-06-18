package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequestMapping
@Controller
public class ItemController {

    @Autowired
    ItemFeignClient itemFeignClient;

    @Value("${testName}")
    private String testName = "";



    /**
     * sku详情页面
     * @param skuId
     * @param model
     * @return
     */
    @RequestMapping("{skuId}.html")
    public String getItem(@PathVariable Long skuId,Model model,ModelMap modelMap){

        // map中包含，skuInfo，List<SpuSaleAttr>，price，BaseCategoryView
        Result<Map> result = itemFeignClient.getItem(skuId);

        // 批量想域中放置map
        model.addAllAttributes(result.getData());

        // modelMap.put("key","value");
        return "item/index";
    }


    @RequestMapping("test")
    public String test(ModelMap modelMap){

        String hello = "hello thymeleaf";

        modelMap.put("hello",hello);

        List<String> list = new ArrayList<>();
        for (int i = 0; i <5 ; i++) {
            list.add("元素"+i);
        }

        modelMap.put("list",list);

        modelMap.put("isChecked","1");


        modelMap.put("hb","红包");

        modelMap.put("gname","<span style=\"color:green\">宝强</span>");

        return "test";
    }

    @RequestMapping("testItem")
    @ResponseBody
    public String testItem(){

        // 通过feign调用service-item
        // Result item = itemFeignClient.testItemApi();
        System.out.println(testName);

        return "itemController";
    }

}
