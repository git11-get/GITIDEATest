package com.atguigu.gmall.portal.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.to.es.EsProduct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
public class ProductItemController {

    @Reference
    private ProductService productService;

    @Autowired
    @Qualifier("mainThreadPoolExecutor")
    private ThreadPoolExecutor mainThreadPoolExecutor;
    @Autowired
    @Qualifier("otherThreadPoolExecutor")
    private ThreadPoolExecutor otherThreadPoolExecutor;

    /**
     * 数据库（商品的基本信息表、商品的属性表、商品的促销表）和es(info、attr、sale)
     * @return
     */
    public EsProduct productInfo2(Long id){

        /*
        1. 商品的基本数据（名字介绍等）
        2. 商品的属性数据
        3. 商品的营销数据
        4. 商品的的配送数据
        5. 商品的增值服务数据
         */

        CompletableFuture.supplyAsync(()->{
           return "";
        },mainThreadPoolExecutor).whenComplete((r,e)->{
            System.out.println("处理结果：="+r);
            System.out.println("处理异常：="+e);
        });


        //1. 商品的基本数据（名字介绍等）
        /*new Thread(()->{
            System.out.println("查基本信息");
        }).start();*/

        //2. 商品的属性数据
        new Thread(()->{
            System.out.println("查属性信息");
        }).start();

        //3. 商品的营销数据
        new Thread(()->{
            System.out.println("查营销信息");
        }).start();

        //4. 商品的的配送数据
        new Thread(()->{
            System.out.println("查配送信息");
        }).start();

        //5. 商品的增值服务数据
        new Thread(()->{
            System.out.println("查增值信息");
        }).start();

        return null;
    }



    /**
     * 商品的详情
     * @param id
     * @return
     */
    @GetMapping("/item/{id}.html")
    public CommonResult productInfo(@PathVariable("id") Long id){

        EsProduct esProduct = productService.productAllInfo(id);
        return new CommonResult().success(esProduct);
    }


    @GetMapping("item/sku/{id}.html")
    public CommonResult productSkuInfo(@PathVariable("id") Long id){
        EsProduct esProduct= productService.productSkuInfo(id);
        return new CommonResult().success(esProduct);
        //return esProduct;
    }
}
