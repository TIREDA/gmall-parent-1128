package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo){
        UserInfo info = userService.login(userInfo);

        if (info != null){
            String token = UUID.randomUUID().toString().replaceAll("-", "");
            HashMap<String, Object> map = new HashMap<>();
            map.put("name",info.getName());
            map.put("nickName",info.getNickName());
            map.put("token",token);
            //token写入缓存
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,info.getId().toString(),RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            //token写入cookie
            return Result.ok(map);
        }else {
            return Result.fail().message("用户名或密码错误");
        }
    }


    //退出登录
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + request.getHeader("token"));
        return Result.ok();
    }

}
