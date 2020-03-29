package com.atguigu.gmall.oms.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.vo.CartItem;
import com.atguigu.gmall.constant.OrderStatusEnume;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.oms.component.MemberComponent;
import com.atguigu.gmall.oms.config.AlipayConfig;
import com.atguigu.gmall.oms.entity.Order;
import com.atguigu.gmall.oms.entity.OrderItem;
import com.atguigu.gmall.oms.mapper.OrderItemMapper;
import com.atguigu.gmall.oms.mapper.OrderMapper;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.service.SkuStockService;
import com.atguigu.gmall.to.es.EsProduct;
import com.atguigu.gmall.to.es.EsProductAttributeValue;
import com.atguigu.gmall.to.es.EsSkuProductInfo;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.ums.entity.MemberReceiveAddress;
import com.atguigu.gmall.ums.service.MemberService;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.atguigu.gmall.vo.order.OrderCreateVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author Lfy
 * @since 2020-03-07
 */
@Service
@Slf4j
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {


    @Reference
    private MemberService memberService;
    @Reference
    private CartService cartService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private MemberComponent memberComponent;
    @Autowired
    private OrderMapper orderMapper;
    @Reference
    private ProductService productService;
    @Reference
    private SkuStockService skuStockService;
    @Autowired
    private OrderItemMapper orderItemMapper;

    private ThreadLocal<List<CartItem>> threadLocal = new ThreadLocal<>();

    @Override
    public OrderConfirmVo orderConfirm(Long id) {
        //1. 获取上一步隐式传参带来的accessToken
        String accessToken = RpcContext.getContext().getAttachment("accessToken");

        OrderConfirmVo confirmVo = new OrderConfirmVo();
        //设置会员收货地址
        confirmVo.setAddresses(memberService.getMemberAddress(id));

        //设置购物券信息
        confirmVo.setCoupons(null);

        //设置购物项
        List<CartItem> cartItems = cartService.getCartItemForOrder(accessToken);
        confirmVo.setItems(cartItems);

        String token = UUID.randomUUID().toString().replace("-", "");

        //给令牌加上业务的过期时间
        token = token+"_"+System.currentTimeMillis()+"_"+10*60*1000;

        //通过redis保存防重令牌
        stringRedisTemplate.opsForSet().add(SysCacheConstant.ORDER_UNIQUE_TOKEN,token);
        //设置订单的防重令牌
        confirmVo.setOrderToken(token);

        //运费是远程计算的
        confirmVo.setTransPrice(new BigDecimal("10"));

        //计算价格等
        confirmVo.setCouponPrice(null);

        cartItems.forEach((cartItem)->{
            Integer count = cartItem.getCount();
            confirmVo.setCount(confirmVo.getCount()+count);

            BigDecimal totalPrice = cartItem.getTotalPrice();
            confirmVo.setProductTotalPrice(confirmVo.getProductTotalPrice().add(totalPrice));
        });

        confirmVo.setTotalPrice(confirmVo.getProductTotalPrice().add(confirmVo.getTransPrice()));

        return confirmVo;
    }


    @Transactional
    @Override
    public OrderCreateVo createOrder(BigDecimal frontTotalPrice, Long addressId,String note) {
        //防重复
        String orderToken = RpcContext.getContext().getAttachment("orderToken");
        //验证令牌的第一种失败
        if(StringUtils.isEmpty(orderToken)){
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("此次操作出现错误，请重新尝试");
            return orderCreateVo;
        }

        //验证令牌的第二种失败 : token = token+"_"+System.currentTimeMillis()+"_"+60*10;
        String[] s = orderToken.split("_");
        if(s.length != 3){
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("非法的操作，请重试");
            return orderCreateVo;
        }

        //验证令牌的第三种失败:时间是否超时的问题

        long createTime = Long.parseLong(s[1]);
        long timeout = Long.parseLong(s[2]);
        if(System.currentTimeMillis()-createTime >= timeout){
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("页面超时，请刷新");
            return orderCreateVo;
        }

        Long remove = stringRedisTemplate.opsForSet().remove(SysCacheConstant.ORDER_UNIQUE_TOKEN, orderToken);
        if(remove == 0){
            //令牌非法。remove == 0是非法，remove == 1就是正确的
            OrderCreateVo orderCreateVo = new OrderCreateVo();
            orderCreateVo.setToken("创建失败，请刷新重试");
            return orderCreateVo;
        }


        //1.获取当前会员
        String accessToken = RpcContext.getContext().getAttachment("accessToken");


        Boolean vaildPrice = vaildPrice(frontTotalPrice, accessToken, addressId);
        if(!vaildPrice){

            OrderCreateVo createVo = new OrderCreateVo();
            createVo.setLimit(false);//比价失败
            return createVo;
        }

        Member member = memberComponent.getMemberByAccessToken(accessToken);

        //初始化订单的vo
        OrderCreateVo orderCreateVo = initOrderCreateVo(frontTotalPrice, addressId, accessToken, member);

        //初始化数据库订单信息
        Order order = initOrder(frontTotalPrice, addressId, note, orderCreateVo, member);

        //保存订单:保证订单防重复提交：1.防重令牌 2.数据库幂等（就是订单号添加一个唯一索引）
        orderMapper.insert(order);

        //2.构造/保存订单项---ThreadLocal同一个线程共享数据
        saveOrderItem(order,accessToken);




        return orderCreateVo;
    }

    @Override
    public String pay(String orderSn, String accessToken) {
        Order order = orderMapper.selectOne(new QueryWrapper<Order>().eq("order_sn", orderSn));
        List<OrderItem> orderItems = orderItemMapper.selectList(new QueryWrapper<OrderItem>().eq("order_sn", orderSn));

        String productName = orderItems.get(0).getProductName();
        StringBuffer body = new StringBuffer();
        for (OrderItem orderItem : orderItems) {

            body.append(orderItem.getProductName()).append("<br/>");
        }
        //调用支付宝的支付方法，会返回一个支付页
        String result = payOrder(orderSn, order.getTotalAmount().toString(), "【谷粒商城】-" + productName, body.toString());
        return result;
    }

    @Override
    public String resolvePayResult(Map<String, String> params) {
        boolean signVerified = true;
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, AlipayConfig.alipay_public_key, AlipayConfig.charset,
                    AlipayConfig.sign_type);
            System.out.println("验签：" + signVerified);

        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
        }
        // 商户订单号
        //String out_trade_no = new String(request.getParameter("out_trade_no").getBytes("ISO-8859-1"), "UTF-8");
        String out_trade_no = params.get("out_trade_no");
        // 支付宝流水号
        //String trade_no = new String(request.getParameter("trade_no").getBytes("ISO-8859-1"), "UTF-8");
        String trade_no = params.get("trade_no");
        // 交易状态
        //String trade_status = new String(request.getParameter("trade_status").getBytes("ISO-8859-1"), "UTF-8");
        String trade_status = params.get("trade_status");

        //只要支付成功，支付宝立即通知,比如1秒、5秒、10秒等，自己自定义的
        if (trade_status.equals("TRADE_FINISHED")) {
            //改订单状态
            log.debug("订单【{}】,已经完成...不能再退款。数据库都改了",out_trade_no);
        } else if (trade_status.equals("TRADE_SUCCESS")) {
            //改数据的订单状态
            Order order = new Order();
            order.setStatus(OrderStatusEnume.PAYED.getCode());
            orderMapper.update(order,new UpdateWrapper<Order>().eq("order_sn", out_trade_no));

            log.debug("订单【{}】,已经支付成功...可以退款。数据库都改了",out_trade_no);

        }

        return "success";
    }


    private void saveOrderItem(Order order,String accessToken) {
        List<Long> skuIds = new ArrayList<>();
        //构造/保存订单项
        List<CartItem> cartItems = threadLocal.get();

        List<OrderItem> orderItems = new ArrayList<>();
        cartItems.forEach((cartItem)->{
            skuIds.add(cartItem.getSkuId());
            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getId());
            orderItem.setOrderSn(order.getOrderSn());
            Long skuId = cartItem.getSkuId();

            //查询当前skuId对应的商品信息
            //Product product =  productService.getProductInfoById(skuId);
            EsProduct esProduct = productService.productSkuInfo(skuId);

            List<EsSkuProductInfo> skuProductInfos = esProduct.getSkuProductInfos();
            SkuStock skuStock = new SkuStock();
            String attrValueJsonStr = "";
            for (EsSkuProductInfo skuProductInfo : skuProductInfos) {
                if(skuId == skuProductInfo.getId()){
                    List<EsProductAttributeValue> values = skuProductInfo.getAttributeValues();
                    attrValueJsonStr = JSON.toJSONString(values);
                    BeanUtils.copyProperties(skuProductInfo,skuStock );
                }
            }
            //不需要远程去查，这样会浪费性能
            //SkuStock skuStock = productService.skuInfoById(skuId);

            orderItem.setProductId(esProduct.getId());
            orderItem.setProductPic(esProduct.getPic());
            orderItem.setProductName(esProduct.getName());
            orderItem.setProductBrand(esProduct.getBrandName());
            orderItem.setProductSn(esProduct.getProductSn());
            //当前购物项的价格
            orderItem.setProductPrice(cartItem.getPrice());
            orderItem.setProductQuantity(cartItem.getCount());
            orderItem.setProductSkuId(skuId);
            orderItem.setProductSkuCode(skuStock.getSkuCode());
            orderItem.setProductCategoryId(esProduct.getProductCategoryId());
            orderItem.setSp1(skuStock.getSp1());
            orderItem.setSp2(skuStock.getSp2());
            orderItem.setSp3(skuStock.getSp3());
            orderItem.setProductAttr(attrValueJsonStr);

            orderItems.add(orderItem);
            orderItemMapper.insert(orderItem);

        });
        //3. 清除购物车已经下单的商品
        cartService.removeCartItem(accessToken,skuIds);
    }


    /**
     * 构造订单vo
     * @param frontTotalPrice
     * @param addressId
     * @param accessToken
     * @param member
     * @return
     */
    private OrderCreateVo initOrderCreateVo(BigDecimal frontTotalPrice, Long addressId, String accessToken, Member member) {
        //生成随机号（通过mybatisplus）
        //String timeId = UUID.randomUUID().toString().replace("-", "");
        String timeId = IdWorker.getTimeId();
        OrderCreateVo orderCreateVo = new OrderCreateVo();

        //设置订单号
        orderCreateVo.setOrderSn(timeId);
        //设置收货地址
        orderCreateVo.setAddressId(addressId);

        List<CartItem> cartItems = cartService.getCartItemForOrder(accessToken);
        //设置购物车中的数据
        orderCreateVo.setCartItems(cartItems);
        //设置会员id
        orderCreateVo.setMemberId(member.getId());
        //总价格
        orderCreateVo.setTotalPrice(frontTotalPrice);
        //描述信息
        orderCreateVo.setDetailInfo(cartItems.get(0).getName());
        return orderCreateVo;
    }

    private Order initOrder(BigDecimal frontTotalPrice, Long addressId, String note, OrderCreateVo orderCreateVo, Member member) {
        //加工处理数据
        //保存订单信息
        Order order = new Order();
        order.setMemberId(member.getId());
        order.setOrderSn(orderCreateVo.getOrderSn());
        order.setCreateTime(new Date());
        order.setAutoConfirmDay(7);
        order.setNote(note);
        order.setMemberUsername(member.getUsername());

        //订单总金额
        order.setTotalAmount(frontTotalPrice);
        order.setFreightAmount(new BigDecimal("10.00"));
        order.setStatus(OrderStatusEnume.UNPAY.getCode());

        //设置收货人信息
        MemberReceiveAddress address = memberService.getMemberAddressByAddressId(addressId);
        order.setReceiverName(address.getName());
        order.setReceiverPhone(address.getPhoneNumber());
        order.setReceiverPostCode(address.getPostCode());
        order.setReceiverProvince(address.getProvince());
        order.setReceiverRegion(address.getRegion());
        order.setReceiverCity(address.getCity());
        order.setReceiverDetailAddress(address.getDetailAddress());
        return order;
    }


    private Boolean vaildPrice(BigDecimal frontPrice,String accessToken,Long addressId){
        //1. 拿到购物车
        List<CartItem> cartItems = cartService.getCartItemForOrder(accessToken);
        threadLocal.set(cartItems);
        BigDecimal bigDecimal = new BigDecimal("0");

        //我们的总价必须取库存服务查询出最新价格
        for (CartItem cartItem : cartItems) {
            //bigDecimal = bigDecimal.add(cartItem.getTotalPrice());
            //查出真正价格
            Long skuId = cartItem.getSkuId();
            BigDecimal newPrice = skuStockService.getSkuPriceBySkuId(skuId);
            cartItem.setPrice(newPrice);
            Integer count = cartItem.getCount();
            //当前项的总价
            BigDecimal multiply = newPrice.multiply(new BigDecimal(count.toString()));
            bigDecimal = bigDecimal.add(multiply);

        }
        //2. 根据收货地址计算运费
        BigDecimal tranPrice = new BigDecimal("10");
        BigDecimal totalPrice = bigDecimal.add(tranPrice);
        return totalPrice.compareTo(frontPrice)==0?true:false;
    }





    /**
     * alipay支付
     * @param out_trade_no  订单号
     * @param total_amount  订单总金额
     * @param subject       订单标题
     * @param body          订单描述
     * @return
     */
    private String payOrder(String out_trade_no,
                            String total_amount,
                            String subject,
                            String body) {
        // 1、创建支付宝客户端
        AlipayClient alipayClient = new DefaultAlipayClient(AlipayConfig.gatewayUrl,
                AlipayConfig.app_id,
                AlipayConfig.merchant_private_key, "json",
                AlipayConfig.charset,
                AlipayConfig.alipay_public_key,
                AlipayConfig.sign_type);

        // 2、创建一次支付请求
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_url);

        // 商户订单号，商户网站订单系统中唯一订单号，必填
        // 付款金额，必填
        // 订单名称，必填
        // 商品描述，可空

        // 3、构造支付请求数据
        alipayRequest.setBizContent("{\"out_trade_no\":\"" + out_trade_no + "\"," + "\"total_amount\":\"" + total_amount
                + "\"," + "\"subject\":\"" + subject + "\"," + "\"body\":\"" + body + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");
        String result = "";
        try {
            // 4、请求
            result = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;// 支付跳转页的代码
    }





}










