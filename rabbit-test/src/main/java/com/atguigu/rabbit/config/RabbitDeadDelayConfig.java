package com.atguigu.rabbit.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitDeadDelayConfig {
    /**
     * 普通的交换机
     * @return
     */
    @Bean
    public Exchange delayExchange(){
        return new DirectExchange("user.order.delay.exchange",true,false);
    }

    @Bean
    public Queue delayQueue(){

        Map<String,Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl",10*10000 );//这个队列里面所有消息的过期时间
        arguments.put("x-dead-letter-exchange","user.order.exchange" );//消息死了交给哪个交换机
        arguments.put("x-dead-letter-routing-key","order" );//死信发出去的路由键
        return new Queue("user.order.delay.queue",
                true,
                false,
                false,
                arguments);
    }

    @Bean
    public Binding delayBinding(){
        return new Binding("user.order.delay.queue",
                Binding.DestinationType.QUEUE,
                "user.order.delay.exchange",
                "order_delay",
                null);
    }


    @Bean
    public Exchange deadExchange(){
        return new DirectExchange("user.order.exchange",true,false);
    }

    @Bean
    public Queue deadQueue(){
        return new Queue("user.order.queue",true,false,false,null);
    }

    @Bean
    public Binding deadBinding(){
        return new Binding("user.order.queue",
                Binding.DestinationType.QUEUE,
                "user.order.exchange",
                "order",
                null);
    }
}
