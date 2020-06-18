package com.atguigu.gmall.test.testThread;

import net.bytebuddy.implementation.bytecode.Throw;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class TestCompletableFuture {

    public static void main(String[] args) throws ExecutionException, InterruptedException{
        // 创建一个线程操作completableFuture
        CompletableFuture completableFuture = CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Integer get() {
                System.out.println("线程操作开始："+Thread.currentThread().getName() + "\t completableFuture");
                int i = 10 / 1;
                return 1024;
            }
        });

        CompletableFuture future = completableFuture.thenApply(new Function<Integer, Integer>() {
            @Override
            public Integer apply(Integer result) {

                // 接收之前操作的返回结果，处理后，返回结果，如果家了Async则代表开启新线程去执行then
                System.out.println("thenApply方法，上次返回结果:" + result);
                return result * 2;
            }
        });

        future.whenComplete(new BiConsumer<Integer,Throwable>() {
            @Override
            public void accept(Integer result, Throwable throwable) {
                // 当线程操作完成后，相当于finally
                System.out.println("------处理操作结果，最后异步，result="+result);
                System.out.println("------Throwable="+throwable);
            }
        });
        Object resultObject = future.get();//整个任务的返回结果
        System.out.println("主线程get（）打印最终结果"+resultObject);
    }

    public static void a(String[] args) throws ExecutionException, InterruptedException {


        // 创建一个线程操作completableFuture
        CompletableFuture completableFuture = CompletableFuture.supplyAsync(new Supplier<Object>() {
            @Override
            public Object get() {
                System.out.println(Thread.currentThread().getName() + "\t completableFuture");
                int i = 10 / 1;
                return 1024;
            }
        });


        // completableFuture任务完成之后的异常处理，一般出现异常时执行
        completableFuture.exceptionally(new Function<Throwable, Object>() {
            @Override
            public Object apply(Throwable throwable) {
                System.out.println("计算异常");
                return "计算异常";
            }
        });

        // completableFuture任务完成之后的回调函数，一般最后执行
        completableFuture.whenComplete(new BiConsumer<Object,Throwable>() {
            @Override
            public void accept(Object o , Throwable throwable) {
                System.out.println("异常处理结果为："+throwable);
                System.out.println("计算结果为："+o);
            }
        });

        Object o = completableFuture.get();

        System.out.println(o);



    }
}
