package com.atguigu.gmall.cart.vo;


import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 *购物项
 */

@Setter
public class CartItem implements Serializable {

    @Getter
    private Long skuId; //skuId
    //当前购物项的基本信息
    @Getter
    private String name;
    @Getter
    private String skuCode;
    @Getter
    private Integer stock;
    @Getter
    private String sp1;
    @Getter
    private String sp2;
    @Getter
    private String sp3;
    @Getter
    private String pic;
    @Getter
    private BigDecimal price; //单价
    @Getter
    private BigDecimal promotionPrice;

    //以上是购物项的基本信息
    @Getter
    private Integer count; //购物项里有多少个
    @Getter
    private Boolean check = true;  //购物项的选中状态


    private BigDecimal totalPrice; //当前购物项总价

    public BigDecimal getTotalPrice() {
        BigDecimal bigDecimal = price.multiply(new BigDecimal(count.toString()));
        return bigDecimal;
    }

}














