package com.atguigu.gmall.portal.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.rpc.RpcContext;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.vo.order.OrderConfirmVo;
import com.atguigu.gmall.vo.order.OrderCreateVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Api(tags = "订单服务")
@RequestMapping("/order")
@RestController
@Slf4j
public class OrderController {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Reference
    private OrderService orderService;


    /**
     * 当信息确认完成以后下一步要提交订单，我们必须做防重复验证（也称为接口幂等性设计）
     *
     *  以前我们利用防重的令牌机制，以后也是用他做接口幂等性设计
     *  我们一般不做令牌的防重幂等性
     *
     *  接口幂等性设计：
     *      select:天然的不需要
     *      insert/delete/update:需要幂等性设计
     * @param accessToken
     * @return
     */
    @ApiOperation("订单确认")
    @RequestMapping("/confirm")
    public CommonResult confirmOrder(@RequestParam("accessToken") String accessToken){
        /**
         * 返回如下数据：
         *  1. 当前用户的可选地址列表
         *  2. 当前购物车选中的商品信息
         *  3. 可用的优惠券信息
         *  4. 支付、配送、发票方式信息
         */

        //0. 检查用户是否存在
        String memberJson = stringRedisTemplate.opsForValue().get(SysCacheConstant.LOGIN_MEMBER + accessToken);
        if(StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(memberJson)){
            //用户未登录
            CommonResult failed = new CommonResult().failed();
            failed.setMessage("用户未登录，请先登录...");
            return failed;
        }

        //1. 登录的用户
        Member member = JSON.parseObject(memberJson, Member.class);

        //dubbo的RPC隐式传参
        RpcContext.getContext().setAttachment("accessToken", accessToken);
        //调用下一个远程服务
        OrderConfirmVo confirm = orderService.orderConfirm(member.getId());
        return new CommonResult().success(confirm);
    }


    @ApiOperation("下单")
    @RequestMapping("/create")
    public CommonResult createOrder(@RequestParam("totalPrice") BigDecimal totalPrice,
                                    @RequestParam("accessToken") String accessToken,
                                    @RequestParam("addressId") Long addressId,
                                    @RequestParam(value = "note",required = false) String note,
                                    @RequestParam("orderToken") String orderToken){
        RpcContext.getContext().setAttachment("accessToken", accessToken);
        //rpc隐式的  需防重复
        RpcContext.getContext().setAttachment("orderToken", orderToken);
        //创建订单要生成订单（总额）和订单项（购物车中商品）
        OrderCreateVo orderCreateVo = orderService.createOrder(totalPrice,addressId,note);

        if(!StringUtils.isEmpty(orderCreateVo.getToken())){
            CommonResult result = new CommonResult().failed();
            result.setMessage(orderCreateVo.getToken());
            return result;
        }
        return new CommonResult().success(orderCreateVo);
    }



    @ApiOperation("支付")
    @RequestMapping(value = "/pay",produces = "text/html")
    @ResponseBody
    public String pay(@RequestParam("orderSn") String orderSn,
                           @RequestParam("accessToken") String accessToken){

        String string = orderService.pay(orderSn,accessToken);
        return string;
    }

    @ResponseBody
    @RequestMapping("/pay/success/async")
    public String paySuccess(HttpServletRequest request) throws UnsupportedEncodingException {

        //log.debug("支付宝支付异步通知进来....");

        //封装支付宝数据
        Map<String, String> params = new HashMap<String, String>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            // 乱码解决，这段代码在出现乱码时使用
            valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        log.debug("订单【{}】==支付宝支付异步通知进来....",params.get("out_trade_no"));

        String result = orderService.resolvePayResult(params);

        return result;

    }

}


























