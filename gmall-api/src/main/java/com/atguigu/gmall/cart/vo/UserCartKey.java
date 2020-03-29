package com.atguigu.gmall.cart.vo;


import lombok.Data;

import java.io.Serializable;

@Data
public class UserCartKey implements Serializable {
    private Boolean login;      //用户是否登录
    private Long userId;        //用户如果登录的id
    private String tempCartKey; //用户没有登录且没有购物车的临时购物车key
    private String finalCartKey;//用户最终用哪个购物车的key
}
