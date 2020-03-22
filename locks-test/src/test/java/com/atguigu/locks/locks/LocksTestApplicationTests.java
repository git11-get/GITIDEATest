package com.atguigu.locks.locks;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LocksTestApplicationTests {

    @Autowired
    private JedisPool jedisPool;

    @Test
    public void contextLoads() {


        System.out.println(jedisPool);
        Jedis jedis = jedisPool.getResource();

        jedis.set("hehe","woowo");

        System.out.println(jedis.get("hehe"));

    }

}
