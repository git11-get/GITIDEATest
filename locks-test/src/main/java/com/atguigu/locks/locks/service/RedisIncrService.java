package com.atguigu.locks.locks.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

@Service
public class RedisIncrService {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;


    @GetMapping("/incr")
    public void incr(){
        stringRedisTemplate.opsForValue().increment("num");

    }


}
