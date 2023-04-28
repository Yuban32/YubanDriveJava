package com.yuban32.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


/**
 * @author Yuban32
 * @ClassName RedisTemplateConfig
 * @Description 解决乱码
 * @Date 2023年03月04日
 */
@Configuration
public class RedisTemplateConfig {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Bean
    public RedisTemplate<String, String> redisTemplateInit() {
        // 设置序列化 Key 的实例对象
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // 设置序列化 value 的实例对象
        redisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return redisTemplate;
    }
}
