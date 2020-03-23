package com.atguigu.gmall.portal.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Configuration
public class ThreadPoolConfig {

    @Bean("mainThreadPoolExecutor")
    public ThreadPoolExecutor mainThreadPoolExecutor(PoolProperties poolProperties){


        LinkedBlockingQueue<Runnable> deQueue = new LinkedBlockingQueue<>(poolProperties.getQueueSize());

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolProperties.getCoreSize(),
                poolProperties.getMaximumPoolSize(),
                10,
                TimeUnit.MINUTES,
                deQueue);

        return threadPoolExecutor;
    }


    @Bean("otherThreadPoolExecutor")
    public ThreadPoolExecutor otherThreadPoolExecutor(PoolProperties poolProperties){

        LinkedBlockingQueue<Runnable> deQueue = new LinkedBlockingQueue<>(poolProperties.getQueueSize());

        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(poolProperties.getCoreSize(),
                poolProperties.getMaximumPoolSize(),
                10,
                TimeUnit.MINUTES,
                deQueue);

        return threadPoolExecutor;
    }

}
