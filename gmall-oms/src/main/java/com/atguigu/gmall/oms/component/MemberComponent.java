package com.atguigu.gmall.oms.component;


import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class MemberComponent {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public Member getMemberByAccessToken(String accessToken){
        String json = stringRedisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        if(!StringUtils.isEmpty(json)){
            Member member = JSON.parseObject(json, Member.class);
            return member;
        }
        return null;
    }
}
