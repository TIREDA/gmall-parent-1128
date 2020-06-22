package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class PassportController {

    @RequestMapping("login.html")
    public String login(String originUrl, ModelMap modelMap){
        modelMap.put("originUrl",originUrl);

        return "login";
    }
}
