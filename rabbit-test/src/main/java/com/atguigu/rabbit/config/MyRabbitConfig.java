package com.atguigu.rabbit.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MyRabbitConfig {

    @Bean
    public Queue helloQueue(){
        //return new Queue("helloQueue",true,false,false,null);
        return new Queue("order-queue",true,false,false,null);
    }

    @Bean
    public Exchange orderExchange(){

        DirectExchange directExchange = new DirectExchange("order-exchange", true, false, null);
        return directExchange;

    }

    /**
     * 第一个参数：队列名称
     * 第二个参数：绑定类型，如有queue、exchange等
     * 第三个参数：交换机
     * 第四个参数：路由键
     * 第五个参数：其他参数
     *
     * @return
     */
    @Bean
    public Binding orderBinding(){

        Binding binding = new Binding("order-queue", Binding.DestinationType.QUEUE, "order-exchange", "createOrder", null);
        return binding;
    }
}
