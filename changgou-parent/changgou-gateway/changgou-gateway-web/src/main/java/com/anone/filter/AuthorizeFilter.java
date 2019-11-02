package com.anone.filter;

import com.anone.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

/**
 * 配置全局过滤
 * 优化：
 * 由于网关不再对令牌的合法进行校验，而是判断令牌是否存在
 * 令牌的校验是在用户访问的服务中进行公钥进行校验
 */
@Component
public class AuthorizeFilter implements GlobalFilter,Ordered {
    //定义令牌的名字
    private static final String AUTHORIZE_TOKEN = "Authorization";
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求和响应对象
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
       //获取请求url
        String path = request.getURI().getPath();
       /* if (path.startsWith("/api/user/login")) {
            return chain.filter(exchange);
        }*/
       //判断当前的uri是否需要校验
        if (!URLFilter.hasAuthorize(path) ||path.startsWith("/api/user/login")) {
            chain.filter(exchange);
        }


        //头文件中获取令牌数据
        String token = request.getHeaders().getFirst(AUTHORIZE_TOKEN);
        System.out.println("请求头的令牌："+token);
        //设置boolean值来判断头是否有令牌
        boolean flag=true;
        //判断token是否为null
        if (StringUtils.isEmpty(token)) {
            //从请求体的参数中获取
            token = request.getQueryParams().getFirst(AUTHORIZE_TOKEN);
            flag=false;
            System.out.println("请求体参数的令牌："+token);
        }
        if (StringUtils.isEmpty(token)) {
            //从cookie中获取
            HttpCookie cookie = request.getCookies().getFirst(AUTHORIZE_TOKEN);
            if (cookie != null) {
                token = cookie.getValue();
                System.out.println("cookie中的令牌："+token);
            }
        }
        if (StringUtils.isEmpty(token)) {
            //没有令牌，拦截
            //设置401 无权限访问
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            //响应空内容
            return response.setComplete();
        } else {
            //存在token，判断token的是否以bearer 开头，如果是则存入头文件中，不是则拼接后再进行存
            if (token.startsWith("bearer") || token.startsWith("Bearer ")) {
                //将令牌信息响应到头中
                request.mutate().header(AUTHORIZE_TOKEN, token);
            } else {
                request.mutate().header(AUTHORIZE_TOKEN,"bearer "+token);
            }
        }


    //放行
        return chain.filter(exchange);
    }

    /**
     * 执行的优先级，越小就执行的越快
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
