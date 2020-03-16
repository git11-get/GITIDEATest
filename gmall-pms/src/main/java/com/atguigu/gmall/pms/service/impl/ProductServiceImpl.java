package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.vo.PageInfoVo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * 商品信息 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-03-07
 */
@Service
@Slf4j
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private ProductAttributeValueMapper productAttributeValueMapper;
    @Autowired
    private ProductFullReductionMapper productFullReductionMapper;
    @Autowired
    private ProductLadderMapper productLadderMapper;
    @Autowired
    private SkuStockMapper skuStockMapper;

    //当前线程共享同一的数据 propagation 传播行为
    public static ThreadLocal<Long> threadLocal = new ThreadLocal<Long>();

    @Override
    public PageInfoVo productPageInfo(PmsProductQueryParam param) {

        QueryWrapper<Product> wrapper = new QueryWrapper<>();

        if(param.getBrandId() !=null){
            //前端传了品牌
            wrapper.eq("brand_id", param.getBrandId());
        }
        if(!StringUtils.isEmpty(param.getKeyword())){
            wrapper.like("name", param.getKeyword());
        }
        if(param.getProductCategoryId() != null){
            wrapper.eq("product_category_id", param.getProductCategoryId());
        }
        if(!StringUtils.isEmpty(param.getProductSn())){
            wrapper.like("product_sn", param.getProductSn());
        }
        if(param.getPublishStatus()!= null){
            wrapper.eq("publish_status", param.getPublishStatus());
        }
        if(param.getVerifyStatus() != null){
            wrapper.eq("verify_status",param.getVerifyStatus());
        }

        IPage<Product> page = productMapper.selectPage(new Page<Product>(param.getPageNum(), param.getPageSize()), wrapper);
        PageInfoVo pageInfoVo = new PageInfoVo(page.getTotal(),page.getPages(),param.getPageSize(),page.getRecords(),page.getCurrent());

        return pageInfoVo;
    }

    /**
     * 大保存
     * @param productParam
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void saveProduct(PmsProductParam productParam) {
        /*//1. pms_product    保存商品基本信息
        Product product = new Product();
        BeanUtils.copyProperties(productParam, product);
        productMapper.insert(product);*/
        ProductServiceImpl proxy = (ProductServiceImpl) AopContext.currentProxy();

        //1. pms_product    保存商品基本信息
        proxy.saveBaseInfo(productParam);

        //2. pms_product_attribute_value  保存这个商品对应的所有属性的值
        proxy.saveProductAttributeValue(productParam);

        //3. pms_product_full_reduction    保存商品的满减信息
        proxy.saveFullReduction(productParam);

        //4. pms_product_ladder 满减表 (阶梯价格表)
        proxy.saveProductLadder(productParam);
       /* try{
            proxy.saveProductLadder(productParam);
        }catch (Exception e){
             log.error(e.getMessage());
        }*/


        //5. pms_sku_stock  库存表
        proxy.saveSkuStock(productParam);
    }




    /**
     * 保存商品基础信息
     * @param productParam
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveBaseInfo(PmsProductParam productParam){
        //1. pms_product    保存商品基本信息
        Product product = new Product();
        BeanUtils.copyProperties(productParam, product);
        productMapper.insert(product);
        //mybatis-plus能自动获取到刚才这个数据的自增id
        log.debug("刚才的商品的id：{}",product.getId());
        threadLocal.set(product.getId());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveProductAttributeValue(PmsProductParam productParam) {
        //2. pms_product_attribute_value  保存这个商品对应的所有属性的值
        List<ProductAttributeValue> valueList = productParam.getProductAttributeValueList();
        /*for (ProductAttributeValue item : valueList) {
            item.setProductId(product.getId());
            productAttributeValueMapper.insert(item);
        }*/
        valueList.forEach((item)->{
            item.setProductId(threadLocal.get());
            productAttributeValueMapper.insert(item);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFullReduction(PmsProductParam productParam) {
        //3. pms_product_full_reduction    保存商品的满减信息
        List<ProductFullReduction> fullReductionList = productParam.getProductFullReductionList();
        fullReductionList.forEach((reduction)->{
            reduction.setProductId(threadLocal.get());
            productFullReductionMapper.insert(reduction);

        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,rollbackFor = FileNotFoundException.class)
    public void saveProductLadder(PmsProductParam productParam)  {
        //4. pms_product_ladder 满减表 (阶梯价格表)
        List<ProductLadder> productLadderList = productParam.getProductLadderList();
        productLadderList.forEach((productLadder)->{
            productLadder.setProductId(threadLocal.get());
            productLadderMapper.insert(productLadder);
        });
        //int i = 1/0;
        //File aaa = new File("aaa");
        //new FileInputStream(aaa);

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveSkuStock(PmsProductParam productParam) {
        //5. pms_sku_stock  库存表
        List<SkuStock> skuStockList = productParam.getSkuStockList();
        /*skuStockList.forEach((skuStock)->{
            if(StringUtils.isEmpty(skuStock)){

            }
            skuStock.setProductId(product.getId());
            skuStockMapper.insert(skuStock);
        });*/
        for (int i = 1; i <= skuStockList.size(); i++) {
            SkuStock skuStock = skuStockList.get(i-1);
            if(StringUtils.isEmpty(skuStock.getSkuCode())){
                //skuCode必须有
                //生成规则 商品id_sku自增id   1_1,1_2,1_3,....
                skuStock.setSkuCode(threadLocal.get()+"_"+i);
            }
            skuStock.setProductId(threadLocal.get());
            skuStockMapper.insert(skuStock);
        }
    }


}
























