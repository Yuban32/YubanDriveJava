package com.yuban32.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Yuban32
 * @ClassName CorsConfig
 * @Description 跨域配置
 * @Date 2023年02月22日
 */
@Slf4j
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        log.info("加载跨域配置");
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods(new String[]{"GET", "HEAD", "POST", "PUT", "DELETE", "OPTIONS"})
                .allowCredentials(true)
                .exposedHeaders("Access-Control-Allow-Origin")
                .allowedHeaders("*");
        WebMvcConfigurer.super.addCorsMappings(registry);
    }
}
