package com.yuban32.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Yuban32
 * @ClassName ImagePathConfig
 * @Description 配置缩略图和完整图片显示
 * @Date 2023年04月06日
 */
@Slf4j
@Configuration
public class ImagePathConfig implements WebMvcConfigurer {
    @Value("${base-file-path.file-path}")
    private String filePath;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("加载图片访问资源配置");
        //将/images/**下的请求 直接映射到本地上
        //完整访问例子 host/images/文件名.jpg
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:"+filePath+"/");
    }
}
