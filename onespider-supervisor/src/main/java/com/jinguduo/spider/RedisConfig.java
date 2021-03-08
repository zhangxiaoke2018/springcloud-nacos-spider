package com.jinguduo.spider;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class RedisConfig {
    
    @Value("${onespider.redis.host}")
    private String jobRedisHost;
    
    @Value("${onespider.redis.password}")
    private String password;
    
    @Value("${onespider.redis.max_total}")
    private Integer maxTotal;//最大连接数
    
    @Value("${onespider.redis.max_idle}")
    private Integer maxIdle;//最大空闲连接数
    
    @Value("${onespider.redis.min_idle}")
    private Integer minIdle;//最小空闲连接数

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        JedisPoolConfig config = new JedisPoolConfig();
        factory.setHostName(jobRedisHost);
        factory.setPassword(password);
        config.setMaxTotal(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        factory.setPoolConfig(config);
        return factory;
    }
}
