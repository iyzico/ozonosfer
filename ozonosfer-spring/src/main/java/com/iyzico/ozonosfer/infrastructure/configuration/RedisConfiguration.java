package com.iyzico.ozonosfer.infrastructure.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.Arrays;
import java.util.HashSet;

@Configuration
public class RedisConfiguration {

    @Bean
    @ConditionalOnProperty(value = "redis.sentinel", havingValue = "true")
    public JedisConnectionFactory jedisConnectionFactory(RedisProperties redisProperties) {
        String[] nodes = redisProperties.getSentinel().getNodes().split(",");
        RedisSentinelConfiguration config = new RedisSentinelConfiguration(redisProperties.getSentinel().getMaster(), new HashSet<>(Arrays.asList(nodes)));
        JedisConnectionFactory jedisConnectionFactory = new JedisConnectionFactory(config);
        jedisConnectionFactory.setPassword(redisProperties.getPassword());
        return jedisConnectionFactory;
    }

    @Bean
    @ConditionalOnProperty(value = "redis.sentinel", havingValue = "false")
    public JedisConnectionFactory jedisConnectionFactory() {
        return new JedisConnectionFactory();
    }
}
