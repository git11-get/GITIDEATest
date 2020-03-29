package com.atguigu.rabbit.service;


import com.atguigu.rabbit.RabbitTestApplication;
import com.atguigu.rabbit.bean.Order;
import com.atguigu.rabbit.bean.User;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class UserService {


    //@RabbitListener(queues = "world")
    public void recieveUserMessage(Message message,
                                   User user,
                                   Channel channel) throws IOException {
        //System.out.println("收到的消息是："+message);
        System.out.println("收到的消息是："+user);
        //System.out.println("收到的消息是："+user);

        //拒绝：可以把消息拒绝掉，让rabbitmq再发送给别人
        channel.basicReject(message.getMessageProperties().getDeliveryTag(),true );
    }



    @RabbitListener(queues = "order-queue")
    public void recieveOrder(Order order,Message message,Channel channel) throws IOException {
        System.out.println("监听到新的订单生成...."+order);

        Long skuId = order.getSkuId();
        Integer num = order.getNum();
        System.out.println("库存系统正在扣除【"+skuId+"】商品的数量，此次扣除【"+num+"】件");

        if(num%2==0){
            System.out.println("库存系统扣除【"+skuId+"】库存失败");

            //回复消息处理失败，并且重新入队
            channel.basicNack(message.getMessageProperties().getDeliveryTag(),false ,true );
            throw new RuntimeException("库存扣除失败");
        }
        System.out.println("订单扣除成功---------");
        //回复成功，只回复本条消息
        channel.basicAck(message.getMessageProperties().getDeliveryTag(),false );

    }


    @RabbitListener(queues = {"user.order.queue"})
    public void closeOrder(Order order,Channel channel){
        System.out.println("收到过期订单："+order+"正在关闭订单");
    }
}






















