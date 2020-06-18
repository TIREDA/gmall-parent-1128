package com.atguigu.gmall.test.service.Impl;


import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.test.service.TestService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TestServiceImpl implements TestService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Override
    public  String testLockRedisson() {
        RLock lock = redissonClient.getLock("lock");
        try {
//            lock.lock();//永久
            lock.lock(10, TimeUnit.SECONDS);
            try {
                boolean b = lock.tryLock(100, 10, TimeUnit.SECONDS);
                if (b) {
                    //相当于redis的setnx成功
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();//解锁
        }
        return "0";
    }

    public void  testLuaScript(){
        String threadCard = UUID.randomUUID().toString();
        //执行lua脚本删除key
        DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();//创建lua脚本
        //lua,在get到key后，根据key的具体值删除key
        luaScript.setResultType(Long.TYPE);
        luaScript.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
        stringRedisTemplate.execute(luaScript, Arrays.asList("dbLock"),threadCard);//执行lua脚本

    }

    @Override
    public String testLock(){

        String threadCard = UUID.randomUUID().toString();

        Boolean dbLock = stringRedisTemplate.opsForValue().setIfAbsent("dbLock", "1", RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);

        if (dbLock){
            String num = stringRedisTemplate.opsForValue().get("num");
            if (StringUtils.isNoneBlank(num)){
                Integer i = Integer.parseInt(num);
                i++;
                stringRedisTemplate.opsForValue().set("num",String.valueOf(i));
                System.out.println("目前缓存商品数量："+i);
                //执行lua脚本删除key
                DefaultRedisScript<Long> luaScript = new DefaultRedisScript<>();//创建lua脚本
                //lua,在get到key后，根据key的具体值删除key
                luaScript.setResultType(Long.class);
                luaScript.setScriptText("if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end");
                stringRedisTemplate.execute(luaScript, Arrays.asList("dbLock"),threadCard);//执行lua脚本
                stringRedisTemplate.delete("dbLock");//删除分布式锁
                return String.valueOf(i);
            }else {
                stringRedisTemplate.opsForValue().set("num",String.valueOf(0));
                String threadCardCurrent = stringRedisTemplate.opsForValue().get("dbLock");
                if (threadCardCurrent.equals(threadCard)){
                    stringRedisTemplate.delete("dbLock");
                }
                return "0";
            }

        }else {
            //等待，重新 尝试
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return "0";
    }

}
