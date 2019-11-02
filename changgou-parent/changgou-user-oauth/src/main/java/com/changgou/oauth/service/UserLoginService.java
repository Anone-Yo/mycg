package com.changgou.oauth.service;

import com.changgou.oauth.util.AuthToken;

public interface UserLoginService {

    /**
     * 认证登录
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     */
    AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type);
}
