package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SpuInfo;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product/baseTrademark/")
@CrossOrigin
public class BaseTrademarkController {

    @Autowired
    BaseTrademarkService baseTrademarkService;

    @RequestMapping("getTrademarkList")
    public Result getTrademarkList(){

        List<BaseTrademark> baseTrademarks =  baseTrademarkService.getTrademarkList();

        return Result.ok(baseTrademarks);
    }


    @ApiOperation(value = "分页列表")
    @GetMapping("{page}/{limit}")
    public Result index(
            @ApiParam(name = "page", value = "当前页码", required = true)
            @PathVariable Long page,

            @ApiParam(name = "limit", value = "每页记录数", required = true)
            @PathVariable Long limit) {
        Page<BaseTrademark> baseTrademarkPage = new Page<>(page,limit);
        IPage<BaseTrademark> iPage =  baseTrademarkService.selectPage(baseTrademarkPage);

        return Result.ok(iPage);
    }


}
