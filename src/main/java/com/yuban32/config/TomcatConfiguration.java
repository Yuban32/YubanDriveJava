package com.yuban32.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.util.UrlPathHelper;

/**
 * @author Yuban32
 * @ClassName TomcatConfiguration
 * @Description
 * @Date 2023年05月18日
 */
@Configuration
public class TomcatConfiguration {

    /**
     * @description 此配置是为了解决用户在上传文件时,文件有特殊字符导致报错的配置 允许tomcat处理特殊字符
     * @param
     * @return TomcatServletWebServerFactory
     **/
    @Bean
    public TomcatServletWebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        factory.addConnectorCustomizers((Connector connector) -> {
            connector.setProperty("relaxedPathChars", "\"<>[\\]^`{|}");
            connector.setProperty("relaxedQueryChars", "\"<>[\\]^`{|}");
        });
        return factory;
    }
}
