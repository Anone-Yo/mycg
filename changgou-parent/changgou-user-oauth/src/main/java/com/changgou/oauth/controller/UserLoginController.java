package com.changgou.oauth.controller;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import entity.Result;
import entity.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user1")
public class UserLoginController {
    //客户端id
    @Value("clientId")
    private String clientId;
    //客户端秘钥
    @Value("clientSecret")
    private String clientSecret;

    @Autowired
    private UserLoginService userLoginService;
    @RequestMapping("/login")
    public Result login(String username,String password) {
        String grant_type="password";
        try {
            AuthToken authToken = userLoginService.login(username, password, clientId, clientSecret, grant_type);
            return new Result(true, StatusCode.OK,"登录成功",authToken);
        } catch (Exception e) {
           return new Result(false,StatusCode.LOGINERROR,"登录失败");
        }

    }
}
