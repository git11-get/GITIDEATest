package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.ProductAttributeCategory;
import com.atguigu.gmall.pms.mapper.ProductAttributeCategoryMapper;
import com.atguigu.gmall.pms.service.ProductAttributeCategoryService;
import com.atguigu.gmall.vo.PageInfoVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <p>
 * 产品属性分类表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-03-07
 */
@Service
public class ProductAttributeCategoryServiceImpl extends ServiceImpl<ProductAttributeCategoryMapper, ProductAttributeCategory> implements ProductAttributeCategoryService {

    @Autowired
    private ProductAttributeCategoryMapper productAttributeCategoryMapper;

    @Override
    public PageInfoVo productAttributeCategoryPageInfo(Integer pageNum, Integer pageSize) {
        IPage<ProductAttributeCategory> page = productAttributeCategoryMapper.selectPage(new Page<ProductAttributeCategory>(pageNum, pageSize), null);
        //返回分页数据对象
        return PageInfoVo.getVo(page,pageSize.longValue());
    }
}