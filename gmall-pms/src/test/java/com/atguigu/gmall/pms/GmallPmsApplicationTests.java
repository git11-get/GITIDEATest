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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.test.context.junit4.SpringRunner;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPmsApplicationTests {

    @Autowired
    private ProductService productService;
    @Autowired
    private BrandService brandService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Test
    public void contextLoads() {

      /*  Brand brand = new Brand();
        brand.setName("哈哈哈s");
        brandService.save(brand);*/


        Brand byId = brandService.getById(53);
        String name = byId.getName();
        System.out.println("--------????????-----"+name);

    }


    @Test
    public void contextLoads1() {
        /*stringRedisTemplate.opsForValue().set("hello", "world");
        System.out.println("保存了数据。。。");
        String hello = stringRedisTemplate.opsForValue().get("hello");
        System.out.println("刚才保存的值是：------- ："+hello);*/

       /* Brand brand = new Brand();
        brand.setName("沃店三卡");
        redisTemplate.opsForValue().set("abc", brand);
        System.out.println("保存了数据。。。");

        Brand abc = (Brand) redisTemplate.opsForValue().get("abc");

        System.out.println(abc.getName());
*/


        Brand brand = new Brand();
        brand.setName("女儿国国主");
        redisTemplate.opsForValue().set("ttt", brand);
        System.out.println("保存了数据。。。");

        Brand ttt = (Brand) redisTemplate.opsForValue().get("ttt");

        System.out.println(ttt.getName());









    }




    }
