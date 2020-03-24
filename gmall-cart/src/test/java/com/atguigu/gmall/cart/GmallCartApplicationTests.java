package com.atguigu.gmall.cart;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.CartItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.Arrays;


@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallCartApplicationTests {

    @Autowired
    private RedissonClient redissonClient;

    @Test
    public void userRedissonMap(){

        RMap<String, String> cart = redissonClient.getMap("cart");
        CartItem cartItem = new CartItem();
        cartItem.setPrice(new BigDecimal("12.98"));
        cartItem.setSkuId(1L);
        cartItem.setCount(1);
        String cartItemJson = JSON.toJSONString(cartItem);
        cart.put("2",cartItemJson);
    }

    @Test
    public void getMap(){
        RMap<String, String> cart = redissonClient.getMap("cart");
        //System.out.println("啦啦啦"+cart.get("2"));
        cart.remove("2");

    }

    @Test
    public void contextLoads() {

        CartItem cartItem = new CartItem();
        cartItem.setCount(2);
        cartItem.setPrice(new BigDecimal("10.98"));

        CartItem cartItem2 = new CartItem();
        cartItem2.setCount(1);
        cartItem2.setPrice(new BigDecimal("11.3"));

        Cart cart = new Cart();
        cart.setCartItems(Arrays.asList(cartItem,cartItem2));

        System.out.println(cart.getCount());
        System.out.println(cart.getTotalPrice());


    }

}


























