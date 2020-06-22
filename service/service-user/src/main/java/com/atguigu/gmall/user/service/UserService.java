package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

public interface UserService {
    UserInfo login(UserInfo userInfo);

    void putUserToken(String token ,String userId);

    String getUserIdByToken(String token);

}
