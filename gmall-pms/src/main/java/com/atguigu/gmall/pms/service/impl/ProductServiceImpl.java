package com.atguigu.gmall.pms.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.constant.EsConstant;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.to.es.EsProductAttributeValue;
import com.atguigu.gmall.to.es.EsSkuProductInfo;
import com.atguigu.gmall.vo.PageInfoVo;
import com.atguigu.gmall.vo.product.PmsProductParam;
import com.atguigu.gmall.vo.product.PmsProductQueryParam;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.searchbox.client.JestClient;
import io.searchbox.core.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
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
    @Autowired
    private JestClient jestClient;

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


    @Override
    public void updatePublishStatus(List<Long> ids, Integer publishStatus) {
        if(publishStatus == 0){
            //下架
            ids.forEach((id)->{
                //修改数据库的状态和删es
                setProductPublishStatus(publishStatus, id);
                //删es
                deleteProductFromEs(id);
            });

        }else {
            //1.对于数据库是修改商品的状态码
            //上架
            ids.forEach((id)->{
                //修改数据库的状态
                setProductPublishStatus(publishStatus, id);
                //添es
                saveProductToEs(id);
            });
        }


    }

    //es下架
    private void deleteProductFromEs(Long id) {

        try {
            Delete delete = new Delete.Builder(id.toString())
                    .index(EsConstant.PRODUCT_ES_INDEX)
                    .type(EsConstant.PRODUCT_INFO_ES_TYPE)
                    .build();
            DocumentResult execute = jestClient.execute(delete);
            if(execute.isSucceeded()){
                log.info("商品:{}-->ES下架完成",id);

           }else {
                log.error("商品:{}-->ES下架失败",id);
                //deleteProductFromEs(id);
            }
        } catch (Exception e) {
            log.error("商品:{}-->ES下架失败",id);
            //deleteProductFromEs(id);
        }

    }

    /*//es上架 -- 我的
    private void saveProductToEs(Long id) {
        //查出商品的基本信息
        Product productInfo = productInfo(id);
        EsProduct esProduct = new EsProduct();

        //1. 复制基本信息
        BeanUtils.copyProperties(productInfo, esProduct);
        //2. 复制sku信息。对于es是要保存商品信息,还要查询出这个商品的sku给es中保存
        List<SkuStock> stocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", id));
        List<EsSkuProductInfo> esSkuProductInfos = new ArrayList<>(stocks.size());
        //查出当前商品的sku属性
        List<ProductAttribute> skuAttributeNames = productAttributeValueMapper.selectProductSaleAttrName(id);
        stocks.forEach((skuStock)->{
            EsSkuProductInfo info = new EsSkuProductInfo();
            BeanUtils.copyProperties(skuStock, info);

            String subTitle = esProduct.getName();
            if(!StringUtils.isEmpty(skuStock.getSp1())){
                subTitle += " "+skuStock.getSp1();
            }
            if(!StringUtils.isEmpty(skuStock.getSp2())){
                subTitle += " "+skuStock.getSp2();
            }
            if(!StringUtils.isEmpty(skuStock.getSp3())){
                subTitle += " "+skuStock.getSp3();
            }
            //sku的特色标题
            info.setSkuTitle(subTitle);
            List<EsProductAttributeValue> skuAttributeValues = new ArrayList<>();

            for (int i = 0; i < skuAttributeNames.size(); i++) {
                //skuAttr 颜色/尺码
                EsProductAttributeValue value = new EsProductAttributeValue();
                value.setName(skuAttributeNames.get(i).getName());
                value.setProductId(id);
                value.setProductAttributeId(skuAttributeNames.get(i).getId());
                value.setType(skuAttributeNames.get(i).getType());
                //颜色、尺码，让es去统计，改掉查询商品的属性分类里面所有属性的时候，按照sort字段排序好
                if(i==0){
                    value.setValue(skuStock.getSp1());
                }
                if(i==1){
                    value.setValue(skuStock.getSp2());
                }
                if(i==2){
                    value.setValue(skuStock.getSp3());
                }

                skuAttributeValues.add(value);
            }
            info.setAttributeValues(skuAttributeValues);
            //sku有多个销售属性：颜色，尺码
            esSkuProductInfos.add(info);
            //查出销售属性的名

        });
        esProduct.setSkuProductInfos(esSkuProductInfos);
        List<EsProductAttributeValue> attributeValues = productAttributeValueMapper.selectProductBaseAttrAndValue(id);
        //3. 复制公共属性信息，查出这个商品的公共属性
        esProduct.setAttrValueList(attributeValues);

        //把商品保存到es中
        try {
            Index build = new Index.Builder(esProduct)
                    .index(EsConstant.PRODUCT_ES_INDEX)
                    .type(EsConstant.PRODUCT_INFO_ES_TYPE)
                    .id(id.toString())
                    .build();
            DocumentResult execute = jestClient.execute(build);
            boolean succeeded = execute.isSucceeded();
            if(succeeded){
                log.info("ES中id为{}商品上架完成",id);
            }else {
                log.error("ES中id为{}商品未保存成功，开始重试",id);
                //saveProductToEs(id);
            }
        } catch (Exception e) {
            log.error("ES中id为：{}，商品数据保存异常:{}",id,e.getMessage());
            //saveProductToEs(id);
        }
    }*/

    private void saveProductToEs(Long id) {
        //1、查出商品的基本新
        Product productInfo = productInfo(id);
        EsProduct esProduct = new EsProduct();


        //1、复制基本信息
        BeanUtils.copyProperties(productInfo,esProduct);


        //2、复制sku信息，对于es要保存商品信息,还要查出这个商品的sku，给es中保存
        List<SkuStock> stocks = skuStockMapper.selectList(new QueryWrapper<SkuStock>().eq("product_id", id));
        List<EsSkuProductInfo> esSkuProductInfos = new ArrayList<>(stocks.size());


        //查出当前商品的sku属性  颜色  尺码
        List<ProductAttribute>  skuAttributeNames = productAttributeValueMapper.selectProductSaleAttrName(id);
        stocks.forEach((skuStock)->{
            EsSkuProductInfo info = new EsSkuProductInfo();
            BeanUtils.copyProperties(skuStock,info);

            //闪亮 黑色
            String subTitle = esProduct.getName();
            if(!StringUtils.isEmpty(skuStock.getSp1())){
                subTitle+=" "+skuStock.getSp1();
            }
            if(!StringUtils.isEmpty(skuStock.getSp2())){
                subTitle+=" "+skuStock.getSp2();
            }
            if(!StringUtils.isEmpty(skuStock.getSp3())){
                subTitle+=" "+skuStock.getSp3();
            }
            //sku的特色标题
            info.setSkuTitle(subTitle);
            List<EsProductAttributeValue> skuAttributeValues = new ArrayList<>();

            for (int i=0;i<skuAttributeNames.size();i++){
                //skuAttr 颜色/尺码
                EsProductAttributeValue value = new EsProductAttributeValue();

                value.setName(skuAttributeNames.get(i).getName());
                value.setProductId(id);
                value.setProductAttributeId(skuAttributeNames.get(i).getId());
                value.setType(skuAttributeNames.get(i).getType());

                //颜色   尺码;让es去统计‘；改掉查询商品的属性分类里面所有属性的时候，按照sort字段排序好
                if(i==0){
                    value.setValue(skuStock.getSp1());
                }
                if(i==1){
                    value.setValue(skuStock.getSp2());
                }
                if(i==2){
                    value.setValue(skuStock.getSp3());
                }

                skuAttributeValues.add(value);

            }


            info.setAttributeValues(skuAttributeValues);
            //sku有多个销售属性；颜色，尺码
            esSkuProductInfos.add(info);
            //查出销售属性的名

        });

        esProduct.setSkuProductInfos(esSkuProductInfos);


        List<EsProductAttributeValue> attributeValues = productAttributeValueMapper.selectProductBaseAttrAndValue(id);
        //3、复制公共属性信息，查出这个商品的公共属性
        esProduct.setAttrValueList(attributeValues);

        try {
            //把商品保存到es中
            Index build = new Index.Builder(esProduct)
                    .index(EsConstant.PRODUCT_ES_INDEX)
                    .type(EsConstant.PRODUCT_INFO_ES_TYPE)
                    .id(id.toString())
                    .build();
            DocumentResult execute = jestClient.execute(build);
            boolean succeeded = execute.isSucceeded();
            if(succeeded){
                log.info("ES中；id为{}商品上架完成",id);
            }else {
                log.error("ES中；id为{}商品未保存成功，开始重试",id);
                //saveProductToEs(id);
            }
        }catch (Exception e){
            log.error("ES中；id为{}商品数据保存异常；{}",id,e.getMessage());
            //saveProductToEs(id);
        }

    }

    public void setProductPublishStatus(Integer publishStatus, Long id) {
        //javabean应该都去用包装类型
        Product product = new Product();
        //默认所有属性值为null
        product.setId(id);
        product.setPublishStatus(publishStatus);
        //这就是mybatis-plus自带的更新方法，是哪个字段有值就更新哪个字段，其他的保持不变
        // 而mybatis则不行
        productMapper.updateById(product);
    }

    @Override
    public Product productInfo(Long id) {
        return productMapper.selectById(id);
    }


    @Override
    public EsProduct productAllInfo(Long id) {

        EsProduct esProduct = null;
        //按照id查出商品
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.termQuery("id", id));

        Search search = new Search.Builder(builder.toString())
                .addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_INFO_ES_TYPE)
                .build();
        try {
            SearchResult execute = jestClient.execute(search);
            List<SearchResult.Hit<EsProduct, Void>> hits = execute.getHits(EsProduct.class);
            esProduct = hits.get(0).source;
        } catch (IOException e) {

        }
        return esProduct;
    }




    @Override
    public EsProduct productSkuInfo(Long id) {

        EsProduct esProduct = null;
        //按照id查出商品
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(QueryBuilders.nestedQuery("skuProductInfos", QueryBuilders.termQuery("skuProductInfos.id",id ), ScoreMode.None));

        Search search = new Search.Builder(builder.toString())
                .addIndex(EsConstant.PRODUCT_ES_INDEX)
                .addType(EsConstant.PRODUCT_INFO_ES_TYPE)
                .build();
        try {
            SearchResult execute = jestClient.execute(search);
            List<SearchResult.Hit<EsProduct, Void>> hits = execute.getHits(EsProduct.class);
            esProduct = hits.get(0).source;
        } catch (IOException e) {

        }
        return esProduct;
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
























