package com.atguigu.rabbit.bean;

import java.io.Serializable;

public class Order implements Serializable {
    private String orderSn;//订单号
    private Long skuId;//购买的商品id
    private Integer num;//购买的个数
    private Integer memeberId;//购买者的id

    public String getOrderSn() {
        return orderSn;
    }

    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }

    public Long getSkuId() {
        return skuId;
    }

    public void setSkuId(Long skuId) {
        this.skuId = skuId;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getMemeberId() {
        return memeberId;
    }

    public void setMemeberId(Integer memeberId) {
        this.memeberId = memeberId;
    }

    public Order(String orderSn, Long skuId, Integer num, Integer memeberId) {
        this.orderSn = orderSn;
        this.skuId = skuId;
        this.num = num;
        this.memeberId = memeberId;
    }

    public Order() {

    }

    @Override
    public String toString() {
        return "Order{" +
                "orderSn='" + orderSn + '\'' +
                ", skuId=" + skuId +
                ", num=" + num +
                ", memeberId=" + memeberId +
                '}';
    }
}
