package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.entity.ProductCategory;
import com.atguigu.gmall.vo.product.PmsProductCategoryWithChildrenItem;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 产品分类 服务类
 * </p>
 *
 * @author Lfy
 * @since 2020-03-07
 */
public interface ProductCategoryService extends IService<ProductCategory> {

    /**
     *查询这个菜单以及它的子菜单
     * @param i
     * @return
     */
    List<PmsProductCategoryWithChildrenItem> listCategorywithChildren(Integer i);
}






