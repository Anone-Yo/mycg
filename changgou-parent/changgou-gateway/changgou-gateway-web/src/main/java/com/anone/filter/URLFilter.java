package com.anone.filter;

/**
 * 设置网关过滤URL
 */
public class URLFilter {
    //购物车订单微服务都需要用户登录，必须携带令牌，所以所有路径都过滤,订单微服务需要过滤的地址
    public static String orderFilterPath = "/api/cart/**,/api/categoryReport/**,/api/orderConfig/**,/api/order/**,/api/orderItem/**,/api/orderLog/**,/api/preferential/**,/api/returnCause/**,/api/returnOrder/**,/api/returnOrderItem/**";

    /**
     * 检查请求是否需要携带令牌就可以访问
     * true 权限校验
     * false 无需校验
     */
    public static boolean hasAuthorize(String uri) {
        //替换所有的**，切割获取一个uri 数组
        String[] urls = orderFilterPath.replace("**", "").split(",");
        //判断拦截的请求
        for (String url : urls) {
            //判断当前是否需要用户权限
            if (uri.startsWith(url)) {
                return true;
            }
        }
        return false;
    }

}
