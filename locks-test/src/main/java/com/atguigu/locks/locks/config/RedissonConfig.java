package com.atguigu.locks.locks.config;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Bean
    public RedissonClient redisson(){
        Config config = new Config();
        /*config.useClusterServers()
                .addNodeAddress("redis://192.168.1.130:6379");*/
        config.useSingleServer()
                .setAddress("redis://192.168.1.130:6379");
        return Redisson.create(config);
    }
}
