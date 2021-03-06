package com.atguigu.gmall.cart.component;


import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.cart.vo.UserCartKey;
import com.atguigu.gmall.constant.CartConstant;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.ums.entity.Member;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Component
public class MemberComponent {

    @Autowired
    private StringRedisTemplate redisTemplate;

    public Member getMemberByAccessToken(String accessToken){

        String userJson = redisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);

        return JSON.parseObject(userJson, Member.class);
    }


    public UserCartKey getCartKey(String accessToken,String cartKey){
        UserCartKey userCartKey = new UserCartKey();

        Member member = null;
        if(!StringUtils.isEmpty(accessToken)){
            member = getMemberByAccessToken(accessToken);
        }

        if(member!=null){
            //获取到了在线用户：用户登录用这个
            userCartKey.setLogin(true);
            userCartKey.setUserId(member.getId());
            userCartKey.setFinalCartKey(CartConstant.USER_CART_KEY_PREFIX+member.getId());
            return userCartKey;
        }else if(!StringUtils.isEmpty(cartKey)){
            //用户有临时的用这个
            userCartKey.setLogin(false);
            userCartKey.setFinalCartKey(CartConstant.TEMP_CART_KEY_PREFIX+cartKey);
            return userCartKey;
        }else{
            //用户既没有登录也没有临时购物车用这个
            String replace = UUID.randomUUID().toString().replace("-", "");
            userCartKey.setLogin(false);
            userCartKey.setFinalCartKey(CartConstant.TEMP_CART_KEY_PREFIX+replace);
            userCartKey.setTempCartKey(replace);
            return userCartKey;
        }

    }
}

