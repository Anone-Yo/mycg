package com.anone.item.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@ControllerAdvice
@Configuration
public class EnableMvcConfig implements WebMvcConfigurer {
    /**
     * 静态资源的放行
     * springmvc配置文件中
     *springmvc:Resource
     * mapping:请求路径的映射
     * location：本地路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/items/**") //mapping:请求路径的映射
                .addResourceLocations("classpath:/items/");  //location：本地路径
    }
}
