package com.changgou.oauth.service.impl;

import com.changgou.oauth.service.UserLoginService;
import com.changgou.oauth.util.AuthToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.Map;

/***
 * 用户登录实现类
 */
@Service
public class UserLoginServiceImpl implements UserLoginService {
    //注入RestTemplate对象
    @Autowired
    private RestTemplate restTemplate;//实现发送请求
    //注入lb client
    @Autowired
    private LoadBalancerClient loadBalancerClient;
    /**
     * 模拟登录
     * @param username
     * @param password
     * @param clientId
     * @param clientSecret
     * @param grant_type
     */
    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret, String grant_type) {
        //封装信息
        //获取请求地址
        ServiceInstance serviceInstance = loadBalancerClient.choose("user-auth");
        String url=serviceInstance.getUri()+"/oauth/token";
        //请求体 封装 账号 密码 权限类型
        MultiValueMap<String,String> paraterMap=new LinkedMultiValueMap<>();
        paraterMap.add("username",username);
        paraterMap.add("password",password);
        paraterMap.add("grant_type",grant_type);

        //封装请求头  请求的格式是Basic base64(客户端id:客户端秘钥)
        String Authorization = "Basic " + new String(Base64.getEncoder().encode((clientId + ":" + clientSecret).getBytes()));
        MultiValueMap<String,String> headerMap=new LinkedMultiValueMap<>();
        headerMap.add("Authorization",Authorization);

        //封装信息，响应
        //参数1：url
        //参数2：提交方式
        //参数3：提交信息的封装
        //参数4：返回数据需要转换的类型
        HttpEntity httpEntity=new HttpEntity(paraterMap,headerMap);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);

        //获取用户登录后的令牌信息
        Map<String,String> map = response.getBody();
        //创建Authorization对象来封装令牌信息
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(map.get("access_token"));
        authToken.setJti(map.get("jti"));
        authToken.setRefreshToken(map.get("refresh_token"));

        return authToken;}
    }
