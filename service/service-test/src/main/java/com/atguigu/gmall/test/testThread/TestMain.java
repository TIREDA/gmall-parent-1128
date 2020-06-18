package com.atguigu.gmall.test.testThread;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;

import java.math.BigDecimal;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class TestMain {

    public static void main(String[] args) throws ExecutionException, InterruptedException {

        // 多线成的实现线程操作，必须实现runnable
        new Thread(new FutureTask<BigDecimal>(new CallableImpl())).start();

        // FutureTask即实现了future，又实现了runnable
        FutureTask<SkuInfo> skuInfoFutureTask = new FutureTask<>(new SkuCallable());
        new Thread(skuInfoFutureTask).start();

        FutureTask<BaseCategoryView> baseCategoryViewFutureTask = new FutureTask<>(new CategoryCallable());
        new Thread(baseCategoryViewFutureTask).start();

        FutureTask<BigDecimal> bigDecimalFutureTask = new FutureTask<>(new PriceCallable());
        new Thread(bigDecimalFutureTask).start();

        SkuInfo skuInfo = skuInfoFutureTask.get();

        BaseCategoryView baseCategoryView = baseCategoryViewFutureTask.get();

        BigDecimal bigDecimal = bigDecimalFutureTask.get();






    }
}
