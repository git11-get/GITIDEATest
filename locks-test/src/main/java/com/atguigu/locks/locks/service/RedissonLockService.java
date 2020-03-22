package com.atguigu.locks.locks.service;


import org.redisson.RedissonReadWriteLock;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedissonLockService {
    @Autowired
    private RedissonClient redissonClient;

    public void lock() {
        RLock lock = redissonClient.getLock("lock");

        //lock.lock();  //默认是阻塞的,就是在等待
        //lock.tryLock();   //是非阻塞的,尝试一下，拿不到就算了



    }

    public void unlock() {
        RLock lock = redissonClient.getLock("lock");
        lock.unlock();
    }

    public String read() {


        RReadWriteLock hehe = redissonClient.getReadWriteLock("hehe");

        return null;
    }



    public Boolean gogogo() {

        RCountDownLatch downLatch = redissonClient.getCountDownLatch("num");
        downLatch.countDown();
        System.out.println("溜了.....");
        return true;
    }


    public Boolean suomen() throws InterruptedException {
        RCountDownLatch downLatch = redissonClient.getCountDownLatch("num");
        downLatch.await();  //等大家都走完.....

        System.out.println("我要锁门....");

        return true;




    }
}
