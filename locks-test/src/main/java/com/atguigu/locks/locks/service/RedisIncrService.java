package com.atguigu.locks.locks.service;


import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.SetParams;

import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Service
public class RedisIncrService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private RedissonClient redisson;



    public void useRedissonForLock(){

        //获取一般锁，只要各个代码用的锁名一样即可
        RLock lock = redisson.getLock("lock");

        //加锁
        try {
            //lock.lock();
            lock.lock(3,TimeUnit.SECONDS );
            Jedis jedis = jedisPool.getResource();
            String num = jedis.get("num");
            Integer i = Integer.parseInt(num);
            i = i+1;
            jedis.set("num",i.toString());
            jedis.close();
        } finally {
            //解锁
            lock.unlock();
        }

    }





    public void incrDistribute2(){

        Jedis jedis = jedisPool.getResource();


        try {
            String token = UUID.randomUUID().toString();
            String lock = jedis.set("lock", token, SetParams.setParams().ex(3).nx());
            if(lock !=null && lock.equalsIgnoreCase("ok")){
                //占位成功，即ok
                String num = jedis.get("num");
                Integer i = Integer.parseInt(num);
                i = i+1;
                jedis.set("num",i.toString());

                //删除锁
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                jedis.eval(script, Collections.singletonList("lock"),Collections.singletonList(token));
                System.out.println("删除锁ok.....");
            }else{
                incrDistribute2();
            }
        } finally {
            jedis.close();

        }


    }




    public void incrDistribute1(){

        //加锁
        String token = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", token, 3, TimeUnit.SECONDS);
        if(lock){
            ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
            String num = stringStringValueOperations.get("num");
            if(num !=null){
                Integer i = Integer.parseInt("num");
                i = i+1;
                stringStringValueOperations.set("num",i.toString());
            }
            //删除锁
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
            DefaultRedisScript<String> script1 = new DefaultRedisScript<>(script);
            //execute是stringRedisTemplate执行脚本的命令
            stringRedisTemplate.execute(script1,Arrays.asList("lock"),token);
            System.out.println("删除锁完成......");
        }else{
            incrDistribute1();
        }

    }


    public void incr(){
        stringRedisTemplate.opsForValue().increment("num");

    }








}
