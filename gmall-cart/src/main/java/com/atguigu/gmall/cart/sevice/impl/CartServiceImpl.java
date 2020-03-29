package com.atguigu.gmall.cart.sevice.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.gmall.cart.component.MemberComponent;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.cart.vo.Cart;
import com.atguigu.gmall.cart.vo.CartItem;
import com.atguigu.gmall.cart.vo.CartResponse;
import com.atguigu.gmall.cart.vo.UserCartKey;
import com.atguigu.gmall.constant.CartConstant;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.pms.entity.Product;
import com.atguigu.gmall.pms.entity.SkuStock;
import com.atguigu.gmall.pms.service.ProductService;
import com.atguigu.gmall.pms.service.SkuStockService;
import com.atguigu.gmall.ums.entity.Member;
import com.mysql.cj.conf.PropertyKey;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;


@Slf4j
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private MemberComponent memberComponent;
    @Autowired
    private  RedissonClient redissonClient;
    @Reference
    private SkuStockService skuStockService;
    @Reference
    private ProductService productService;

    @Override
    public CartResponse addToCart(Long skuId,Integer num,String cartKey, String accessToken) throws ExecutionException, InterruptedException {
        //0. 根据accessToken获取用户的id
        Member member = memberComponent.getMemberByAccessToken(accessToken);
        if(member != null && !StringUtils.isEmpty(cartKey)){
            //先去合并再说
            mergeCart(cartKey,member.getId());

        }
        //获取到用户真正能使用到的购物车
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        CartItem cartItem = addItemToCart(skuId,num,finalCartKey);
        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);

        //设置临时购物车用户的cartKey
        cartResponse.setCartKey(userCartKey.getTempCartKey());

        //维护勾选状态列表
        //checkItem(Arrays.asList(skuId),true ,finalCartKey );

        //返回整个购物车方便操作
        cartResponse.setCart(listCart(cartKey,accessToken ).getCart());
        return cartResponse;
    }





    @Override
    public CartResponse updateCartItem(Long skuId, Integer num, String cartKey, String accessToken) {

        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String,String> map = redissonClient.getMap(finalCartKey);

        String json = map.get(skuId.toString());
        CartItem cartItem = JSON.parseObject(json, CartItem.class);
        cartItem.setCount(num);

        String jsonString = JSON.toJSONString(cartItem);
        map.put(skuId.toString(),jsonString );

        CartResponse cartResponse = new CartResponse();
        cartResponse.setCartItem(cartItem);

        return cartResponse;
    }


    @Override
    public CartResponse listCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        if(userCartKey.getLogin()){
            //用户登录了，就需要合并购物车
            mergeCart(cartKey, userCartKey.getUserId());
        }
        //查询出购物车的数据
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String,String> map = redissonClient.getMap(finalCartKey);
        Cart cart = new Cart();
        List<CartItem> cartItems = new ArrayList<>();
        CartResponse cartResponse = new CartResponse();
        if(map!=null && !map.isEmpty()){
            map.entrySet().forEach((item)->{
                if(!item.getKey().equalsIgnoreCase(CartConstant.CART_CHECKED_KEY)){
                    String value = item.getValue();
                    CartItem cartItem = JSON.parseObject(value, CartItem.class);
                    cartItems.add(cartItem);
                }
            });
            cart.setCartItems(cartItems);
        }else {
            //用户没有购物车，新建一个购物车
            cartResponse.setCartKey(userCartKey.getTempCartKey());
        }
        cartResponse.setCart(cart);
        return cartResponse;
    }

    @Override
    public CartResponse delCartItem(Long skuId, String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();

        //维护购物项的checked状态
        checkItem(Arrays.asList(skuId),false ,finalCartKey );

        //获取购物车删除购物项
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        map.remove(skuId.toString());

        //整个购物车再返回出去
        CartResponse cartResponse = listCart(cartKey, accessToken);
        return cartResponse;
    }

    @Override
    public CartResponse clearCart(String cartKey, String accessToken) {
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        map.clear();
        CartResponse cartResponse = new CartResponse();
        return cartResponse;
    }


   @Override
    public CartResponse checkCartItem(String skuIds, Integer ops, String cartKey, String accessToken) {
        List<Long> skuIdsList = new ArrayList<>();
        UserCartKey userCartKey = memberComponent.getCartKey(accessToken, cartKey);
        String finalCartKey = userCartKey.getFinalCartKey();
        RMap<String, String> cart = redissonClient.getMap(finalCartKey);
        boolean checked = ops==1?true:false;
        //修改购物项状态
        if(!StringUtils.isEmpty(skuIds)){
            String[] ids = skuIds.split(",");
            for (String id : ids) {
                long skuId = Long.parseLong(id);
                skuIdsList.add(skuId);
                //1. 找到每个skuId对应的购物车中的json，把状态ckeck改为ops对应的值
                //找到购物车中这个sku进行状态修改
                if(cart!=null && !cart.isEmpty()){
                    String jsonValue = cart.get(id);
                    //转换成json对象
                    CartItem cartItem = JSON.parseObject(jsonValue, CartItem.class);
                    cartItem.setCheck(checked);
                    //覆盖redis原数据
                    cart.put(id,JSON.toJSONString(cartItem));
                }
            }
        }
        /**
        //修改checked集合的状态
        //2. 为了快速找到哪个被选中了，我们单独维护了数组，数组在map中用的key是ckecked，值是set集合最好
        String checkedJson = cart.get(CartConstant.CART_CHECKED_KEY);
        Set<Long> longSet = JSON.parseObject(checkedJson, new TypeReference<Set<Long>>() {
        });
        //防止空指针
        if(longSet == null || longSet.isEmpty()){
            longSet = new HashSet<>();
        }
        if(checked){
            //如果当前操作都是选中购物项
            longSet.addAll(skuIdsList);
            log.info("被选中的商品{}",longSet );
        }else {
            longSet.removeAll(skuIdsList);
            log.info("被移除不选中的商品{}",longSet );
        }
        //重新保存被选中的商品
        cart.put(CartConstant.CART_CHECKED_KEY,JSON.toJSONString(longSet));
       */

        //修改checked集合的状态
        //2. 为了快速找到哪个被选中了，我们单独维护了数组，数组在map中用的key是ckecked，值是set集合最好
        checkItem(skuIdsList,checked,finalCartKey);
        //3. 返回整个购物车
        CartResponse cartResponse = listCart(cartKey, accessToken);
        return cartResponse;
    }

    @Override
    public List<CartItem> getCartItemForOrder(String accessToken) {

        List<CartItem> cartItems = new ArrayList<>();

        //1、根据用户的accessToken获取到购物车中被选中的数据
        UserCartKey cartKey = memberComponent.getCartKey(accessToken, null);
        RMap<String, String> cart = redissonClient.getMap(cartKey.getFinalCartKey());


        String checkItemsJson = cart.get(CartConstant.CART_CHECKED_KEY);
        Set<Long> items = JSON.parseObject(checkItemsJson, new TypeReference<Set<Long>>() {
        });

        items.forEach((item)->{
            String itemJSON = cart.get(item.toString());
            cartItems.add(JSON.parseObject(itemJSON,CartItem.class));
        });
        return cartItems;
    }

    @Override
    public void removeCartItem(String accessToken, List<Long> skuIds) {
        UserCartKey cartKey = memberComponent.getCartKey(accessToken, null);
        String finalCartKey = cartKey.getFinalCartKey();
        RMap<String, String> map = redissonClient.getMap(finalCartKey);
        skuIds.forEach((id)->{
            //移除商品项
            map.remove(id.toString());
        });
        //移除勾选的状态保存
        map.put(CartConstant.CART_CHECKED_KEY,JSON.toJSONString(new LinkedHashSet<Long>()) );
    }


    /**
     * 添加商品到购物车
     * @param skuId
     * @param num   商品个数
     * @param finalCartKey
     * @return
     */
    private CartItem addItemToCart(Long skuId,Integer num,String finalCartKey) throws ExecutionException, InterruptedException {

        CartItem newCartItem = new CartItem();
        /**
         * 1、只接受上一步的结果
         * thenAccept(r){ //上一步是执行异步，而thenAccept之后是执行同步
         *     r:上一步的结果
         * }
         *
         * 2、thenApply(r){
         *     r：把上一步的结果拿来进行修改再返回，
         * }
         *
         * 3、thenAccpet(){} 上一步结果1s+本次处理2s=3s
         *
         *    //上一步是执行异步，而thenAccpetAsync之后是执行的也是异步
         * 4、thenAccpetAsync(){} 上一步1s+异步2s = 最多等2s
         */

        CompletableFuture<Void> skuFuture = CompletableFuture.supplyAsync(() -> {
            SkuStock skuStock = skuStockService.getById(skuId);
            return skuStock;
        }).thenAcceptAsync((stock)->{
            //拿到上一步的商品id
            Long productId = stock.getProductId();
            Product product = productService.getById(productId);

            //拿到上一步结果整体封装
            BeanUtils.copyProperties(stock,newCartItem);
            newCartItem.setSkuId(stock.getId());
            newCartItem.setName(product.getName());
            newCartItem.setCount(num);

        });

        // 查出skuId在数据库对应的最新详情,远程查询
        //购物车集合key是String,value[里面是购物项]是String【要系列化成json】
        RMap<String,String> map = redissonClient.getMap(finalCartKey);
        //获取购物车中这个skuId对应的购物项
        String itemJson = map.get(skuId.toString());
        skuFuture.get();    //在线等结果
        //检查购物车中是否已经存在这个购物项
        if(!StringUtils.isEmpty(itemJson)){
            //只是数量叠加,购物车老item获取到数量，给新的cartItem里面添加信息
            CartItem oldItem = JSON.parseObject(itemJson, CartItem.class);
            Integer count = oldItem.getCount();
            //等到异步任务完成了，newCartItem才能用
            newCartItem.setCount(count+newCartItem.getCount());
            String string = JSON.toJSONString(newCartItem);
            //老数据覆盖成新数据
            map.put(skuId.toString(),string );
        }else {
            //新增购物项
            String string = JSON.toJSONString(newCartItem);
            map.put(skuId.toString(),string );
        }
        //维护勾选状态列表
        checkItem(Arrays.asList(skuId),true,finalCartKey);
        return newCartItem;

    }




    /**
     *
     * @param cartKey   老购物车
     * @param id 用户id
     */
    private void mergeCart(String cartKey, Long id) {
        String oldCartKey = CartConstant.TEMP_CART_KEY_PREFIX+cartKey;
        String userCartKey = CartConstant.USER_CART_KEY_PREFIX+id.toString();
        //获取到老购物车的数据
        RMap<String, String> map = redissonClient.getMap(oldCartKey);
        if(map !=null && !map.isEmpty()){
            //map不是null，而且里面有数据才需要合并
            map.entrySet().forEach((item)->{
                String key = item.getKey();
                if(!key.equalsIgnoreCase(CartConstant.CART_CHECKED_KEY)){
                    //购物项的json数据
                    String value = item.getValue();
                    CartItem cartItem = JSON.parseObject(value, CartItem.class);
                    try {
                        addItemToCart(Long.parseLong(key), cartItem.getCount(), userCartKey);
                        //移除;;如果数量多了老要向redis提请移除太麻烦，直接用 :map.clear();
                        //map.remove(key);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            map.clear();
        }
    }


    private void checkItem(List<Long> skuId,boolean checked,String finalCartKey){

        RMap<String, String> cart = redissonClient.getMap(finalCartKey);

        //修改checked集合的状态
        //2. 为了快速找到哪个被选中了，我们单独维护了数组，数组在map中用的key是ckecked，值是set集合最好
        String checkedJson = cart.get(CartConstant.CART_CHECKED_KEY);
        Set<Long> longSet = JSON.parseObject(checkedJson, new TypeReference<Set<Long>>() {
        });
        //防止空指针
        if(longSet == null || longSet.isEmpty()){
            longSet = new LinkedHashSet<>();
        }
        if(checked){
            //如果当前操作都是选中购物项
            longSet.addAll(skuId);
            log.info("被选中的商品{}",longSet );
        }else {
            longSet.removeAll(skuId);
            log.info("被移除不选中的商品{}",longSet );
        }
        //重新保存被选中的商品
        cart.put(CartConstant.CART_CHECKED_KEY,JSON.toJSONString(longSet));
    }


}













