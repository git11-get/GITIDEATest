package com.atguigu.locks.locks.controller;


import com.atguigu.locks.locks.service.RedisIncrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {


    @Autowired
    private RedisIncrService redisIncrService;

    @GetMapping("/incr")
    public String incr(){
        redisIncrService.incr();
        return "ok";
    }
    @GetMapping("/incr2")
    public String incr2(){
        redisIncrService.incrDistribute2();
        return "ok";
    }

    @GetMapping("/incr3")
    public String incr3(){
        redisIncrService.useRedissonForLock();
        return "ok";
    }


}
