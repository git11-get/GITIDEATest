package com.atguigu.gmall.constant;

public class CartConstant {
    public static final String TEMP_CART_KEY_PREFIX = "cart:temp:";    //后面加cartKey
    public static final String USER_CART_KEY_PREFIX = "cart:user:";    //后面加用户id
    //购物车在redis中存储哪些被选中用的key
    public static final String CART_CHECKED_KEY = "checked";
}
