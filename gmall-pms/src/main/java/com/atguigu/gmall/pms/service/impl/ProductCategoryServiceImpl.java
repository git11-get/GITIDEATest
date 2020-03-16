package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.pms.mapper.ProductCategoryMapper;
import com.atguigu.gmall.pms.service.ProductCategoryService;
import com.atguigu.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

/**
 * <p>
 * 产品分类 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-03-07
 */
@Slf4j
@Service
public class ProductCategoryServiceImpl extends ServiceImpl<ProductCategoryMapper, ProductCategory> implements ProductCategoryService {


    @Autowired
    private ProductCategoryMapper productCategoryMapper;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    @Override
    public List<PmsProductCategoryWithChildrenItem> listCategorywithChildren(Integer i) {
        Object cacheMenu = redisTemplate.opsForValue().get(SysCacheConstant.CATEGORY_MENU_CACHE_KEY);
        List<PmsProductCategoryWithChildrenItem> items;
        if(cacheMenu!=null){
            //缓存中有
            log.debug("菜单数据命中缓存.....");
            items = (List<PmsProductCategoryWithChildrenItem>) cacheMenu;
        }else {
            items = productCategoryMapper.listCategorywithChildren(i);
            redisTemplate.opsForValue().set(SysCacheConstant.CATEGORY_MENU_CACHE_KEY, items);
        }
        return items;
    }
}
