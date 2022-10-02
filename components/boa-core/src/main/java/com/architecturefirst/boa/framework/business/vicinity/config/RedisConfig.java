package com.architecturefirst.boa.framework.business.vicinity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * Configuration to support Redis
 */
@Configuration
public class RedisConfig {

    @Value("${redis.host:localhost}")
    private String host;

    @Value("${redis.port:6379}")
    private int port;

    @Bean
    public JedisPooled jedisPool() {
        return new JedisPooled(host, port);
    }

}
