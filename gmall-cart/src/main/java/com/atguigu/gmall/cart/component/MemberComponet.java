package com.atguigu.gmall.cart.component;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class MemberComponet {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public Member getMemberByAccessToken(String accessToken){

        String userJson = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);

        return JSON.parseObject(userJson, Member.class);
    }
}
