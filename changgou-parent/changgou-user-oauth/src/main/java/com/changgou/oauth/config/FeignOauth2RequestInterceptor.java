package com.changgou.oauth.config;

import com.changgou.oauth.util.JwtToken;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Enumeration;

/**
 * 创建管理员拦截器，用于携带管理员令牌,将令牌封装到头文件中，请求其他的服务
 */
@Configuration
public class FeignOauth2RequestInterceptor implements RequestInterceptor {
    /****
     * 自定义操作
     */
    @Override
    public void apply(RequestTemplate requestTemplate) {
        try {
            //创建令牌信息
            String token="Bearer "+JwtToken.adminJwt();//创建管理员令牌
            //将令牌添加到头文件中
            requestTemplate.header("Authorization",token);

            //获取security中对象
            //获取request相关变量
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                //取出request
                HttpServletRequest request = attributes.getRequest();
                //获取请求头信息的key
                Enumeration<String> headerNames = request.getHeaderNames();
                if (headerNames != null) {
                    while (headerNames.hasMoreElements()) {
                        //头文件的key
                        String headerName = headerNames.nextElement();
                        //头文件的value
                        String values = request.getHeader(headerName);
                        //将令牌数据添加到头文件中
                        requestTemplate.header(headerName,values);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}