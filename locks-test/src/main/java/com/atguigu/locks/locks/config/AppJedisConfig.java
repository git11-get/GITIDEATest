package com.atguigu.locks.locks.config;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Configuration
public class AppJedisConfig {

    @Bean
    public JedisPool jedisPoolConfig(RedisProperties properties){
        //1. 连接工厂中所有信息都有
        JedisPoolConfig config = new JedisPoolConfig();

        RedisProperties.Pool pool = properties.getJedis().getPool();

        config.setMinIdle(pool.getMaxIdle());
        config.setMaxTotal(pool.getMaxActive());

        JedisPool jedisPool = null;
        jedisPool = new JedisPool(config,properties.getHost(),properties.getPort());
        return jedisPool;
    }

}
