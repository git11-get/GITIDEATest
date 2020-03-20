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


}
