package com.zhao.seckill.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/13 14:01
 * @description redis配置类
 */

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        RedisTemplate<String, Object> stringObjectRedisTemplate = new RedisTemplate<>();

        /** 设置键值的序列化 **/
        stringObjectRedisTemplate.setKeySerializer(new StringRedisSerializer());
        stringObjectRedisTemplate.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        /** 设置hash类型的序列化 **/
        stringObjectRedisTemplate.setHashKeySerializer(new StringRedisSerializer());
        stringObjectRedisTemplate.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        /** 注入连接工厂 **/
        stringObjectRedisTemplate.setConnectionFactory(redisConnectionFactory);
        return stringObjectRedisTemplate;
    }

}
