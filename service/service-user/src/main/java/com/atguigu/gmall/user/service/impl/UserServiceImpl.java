package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserInfoMapper userInfoMapper;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public UserInfo login(UserInfo userInfo) {

        String passwd = userInfo.getPasswd();

        passwd = DigestUtils.md5DigestAsHex(passwd.getBytes());

        String loginName = userInfo.getLoginName();

        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();

        wrapper.eq("passwd",passwd);
        wrapper.eq("login_name",loginName);
        UserInfo userInfoResult = userInfoMapper.selectOne(wrapper);


        return userInfoResult;
    }

    @Override
    public void putUserToken(String token, String userId) {
        redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX+token,userId);
    }

    @Override
    public String getUserIdByToken(String token) {
        String userId = (String)redisTemplate.opsForValue().get(RedisConst.USER_LOGIN_KEY_PREFIX+token);
        return userId;
    }
}
