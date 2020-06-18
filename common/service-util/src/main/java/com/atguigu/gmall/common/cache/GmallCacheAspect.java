package com.atguigu.gmall.common.cache;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Component
@Aspect
public class GmallCacheAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedissonClient redissonClient;


    @Around("@annotation(com.atguigu.gmall.common.cache.GmallCache)")
    public Object cacheAroundAdvice(ProceedingJoinPoint point){
        // 声明一个对象Object,返回结果
        Object result = null;

        // 获得连接点参数
        Object[] args = point.getArgs();

        // 通过反射获得原始方法信息
        MethodSignature signature  =  (MethodSignature)point.getSignature();
        Class returnType = signature.getReturnType();
        GmallCache gmallCache = signature.getMethod().getAnnotation(GmallCache.class);

        // 获得注解信息
        String prefix = gmallCache.prefix();

        // 根据注解拼接缓存key
        String key = prefix + Arrays.asList(args).toString();

        // 缓存代码
        result = cacheHit(returnType, key);

        // 表示缓存不为空，则直接返回数据
        if (result!=null){
            return result;
        }

        // 使用redisson获得分布式锁
        RLock lock = redissonClient.getLock(key + ":lock");

        // 执行连接点方法，执行db
        try {
            // 成功拿到分布式锁的，可以查询db
            boolean b = lock.tryLock(100, 10, TimeUnit.SECONDS);
            if(b){
                result = point.proceed(args);// 执行连接点方法，查询db
                // 如果查询不到数据，将空对象放入缓存，防止缓存穿透
                if(result ==null){
                    redisTemplate.opsForValue().set(key,JSON.toJSONString(new Object()),60*60,TimeUnit.SECONDS);
                    return result;
                }

                // 查询到数据后同步缓存返回结果
                redisTemplate.opsForValue().set(key,JSON.toJSONString(result));

                // 返回结果给原始方法
                return result;
            }else {
                // 如果没有拿到分布式锁，那么说明已经有人查数据库了，当前执行的线程直接取缓存里面拿其他线程已经存入的数据就行了
                Thread.sleep(1000);// 等待其他线程放入数据
                return cacheHit(returnType,key);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }finally {
            lock.unlock();
        }
        return result;// 返回原方法需要的的结果
    }

    /***
     * 查询缓存中的key
     * @param returnType
     * @param key
     * @return
     */
    private Object cacheHit(Class returnType, String key) {
        Object result = null;
        String cache = (String)redisTemplate.opsForValue().get(key);
        if(StringUtils.isNotBlank(cache)){
            result = JSON.parseObject(cache, returnType);
        }
        return result;
    }


}
