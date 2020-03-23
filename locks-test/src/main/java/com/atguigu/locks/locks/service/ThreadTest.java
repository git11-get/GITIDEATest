package com.atguigu.locks.locks.service;

import java.util.UUID;
import java.util.concurrent.*;

public class ThreadTest {


   /* public static void main(String[] args) throws Exception {

        System.out.println("主线程....");
        Thread01 thread01 = new Thread01();

        //异步化
        //new Thread(thread01).start();
        //new Thread(new Thread02()).start();

        FutureTask<String> task = new FutureTask<>(new Thread03());
        new Thread(task).start();
        //获取异步运行的结果
        String s = task.get();
        System.out.println("异步获取到的结果是："+s);

        System.out.println("主线程....结束");
    }*/


    /*public static void main(String[] args) {
        ExecutorService threadPool = Executors.newFixedThreadPool(2);

        System.out.println("线程池任务准备....");
        for (int i = 0; i < 10; i++) {
            Thread thread = new Thread(()->{
                System.out.println("当前线程开始-->:"+Thread.currentThread());
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (Exception e) {
                }

                System.out.println("当前线程结束--》:"+Thread.currentThread());
            });
            //给线程池提交任务
            threadPool.submit(thread);
        }
        System.out.println("所有任务都已提交");
    }*/


    public static void main(String[] args) {
        //CompletableFuture

        ExecutorService threadPool = Executors.newFixedThreadPool(2);
        System.out.println("主线程。。。。。");
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程开始-->:" + Thread.currentThread());
            String uuid = UUID.randomUUID().toString();
            System.out.println("当前线程结束--》:" + Thread.currentThread());
            return uuid;
        }, threadPool);

        System.out.println("主线程。。结束--。。。"+future);
    }



}


class Thread01 extends  Thread{
    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
        }
        System.out.println("Thread01-当前线程"+Thread.currentThread());
    }
}

class Thread02 implements Runnable{
    @Override
    public void run() {
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
        }
        System.out.println("Thread02-当前线程"+Thread.currentThread());
    }
}

class Thread03 implements Callable<String>{

    @Override
    public String call() throws Exception {

        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
        }

        return "ok";
    }
}