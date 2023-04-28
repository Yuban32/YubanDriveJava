package com.yuban32.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author Yuban32
 * @ClassName ImagePathConfig
 * @Description 配置图片显示
 * @Date 2023年04月06日
 */
@Slf4j
@Configuration
public class ImagePathConfig implements WebMvcConfigurer {
    @Value("${base-file-path.file-path}")
    private String filePath;
    @Value("${base-file-path.user-upload-file-path}")
    private String userUploadFilePath;
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("加载图片访问资源配置");

        registry.addResourceHandler("/download/**")
                .addResourceLocations("file:"+filePath+"/");
    }
}
