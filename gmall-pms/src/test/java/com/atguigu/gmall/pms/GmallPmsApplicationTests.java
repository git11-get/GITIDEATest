package com.atguigu.gmall.pms;

import com.atguigu.gmall.pms.entity.Brand;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.service.BrandService;
import com.atguigu.gmall.pms.service.ProductService;
import com.baomidou.mybatisplus.extension.service.additional.query.impl.QueryChainWrapper;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.implementation.bind.MethodDelegationBinder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPmsApplicationTests {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;

    @Test
    public void contextLoads() {

      /*  Brand brand = new Brand();
        brand.setName("哈哈哈s");
        brandService.save(brand);*/


        Brand byId = brandService.getById(53);
        String name = byId.getName();
        System.out.println("--------????????-----"+name);

    }



}
