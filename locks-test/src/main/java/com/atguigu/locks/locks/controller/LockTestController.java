package com.atguigu.locks.locks.controller;


import com.atguigu.locks.locks.service.RedissonLockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LockTestController {

    @Autowired
    private RedissonLockService redissonLockService;



    @GetMapping("/lock")
    public String lock(){
        redissonLockService.lock();
        return "ok";
    }

    @GetMapping("/unlock")
    public String unlock(){
        redissonLockService.unlock();
        return "ok";
    }

    @GetMapping("/read")
    public String read(){
        return redissonLockService.read();
    }




    @GetMapping("/go")
    public Boolean gogogo(){
        return redissonLockService.gogogo();
    }

    @GetMapping("/suomen")
    public String suomen() throws InterruptedException {
        Boolean suomen = redissonLockService.suomen();
        return suomen?"锁门了":"门没锁";
    }
}
