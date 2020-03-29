package com.atguigu.rabbit;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRabbit
public class RabbitTestApplication {

    public static void main(String[] args) {
        SpringApplication.run(RabbitTestApplication.class, args);
    }


    //自定义rabbitmq的MessageConverter
    @Bean
    public MessageConverter getRabbitTemplate(){
        return new Jackson2JsonMessageConverter();
    }
}
