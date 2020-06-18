package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFileClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;
import java.util.jar.Attributes;

@Controller
public class ListController {

    @Autowired
    ListFileClient listFeignClient;

    @RequestMapping({"search.html","list.html"})
    public String list(SearchParam searchParam, ModelMap modelMap, Model model){

        // 通过list服务的api搜索es中的商品数据，放到页面进行渲染
        Result<Map> result = listFeignClient.list(searchParam);// 调用es的api
        model.addAllAttributes(result.getData());

        // modelMap.put("searchParam",searchParam);
        return "list/index";
    }

}
