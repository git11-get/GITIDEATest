package com.atguigu.locks.locks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;

@SpringBootApplication
public class LocksTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(LocksTestApplication.class, args);
    }

}
