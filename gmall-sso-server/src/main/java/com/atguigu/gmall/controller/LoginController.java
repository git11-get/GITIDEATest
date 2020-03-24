package com.atguigu.gmall.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.constant.SysCacheConstant;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.Member;
import com.atguigu.gmall.ums.service.MemberService;
import com.atguigu.gmall.vo.ums.LoginResponseVo;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Controller
public class LoginController {


    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Reference
    private MemberService memberService;


    @RequestMapping("/userinfo")
    @ResponseBody
    public CommonResult getUserInfo(@RequestParam String accessToken){


        String redisKey = SysCacheConstant.LOGIN_MEMBER+accessToken;

        String member = stringRedisTemplate.opsForValue().get(redisKey);
        Member loginMember = JSON.parseObject(member, Member.class);
        loginMember.setId(null);
        loginMember.setPassword(null);

        return new CommonResult().success(loginMember);
    }




    @RequestMapping("/applogin")
    @ResponseBody
    public CommonResult loginForGmall(@RequestParam String username, @RequestParam String password){
        Member member = memberService.login(username,password);
        if(member == null){
            //用户没有
            CommonResult result = new CommonResult().failed();
            result.setMessage("账号命名不匹配，请重新登录...");
            return result;
        }else {
            String token = UUID.randomUUID().toString().replace("-","" );
            String memberJson = JSON.toJSONString(member);
            //成功，保存到缓存中
            stringRedisTemplate.opsForValue().set(SysCacheConstant.LOGIN_MEMBER+token, memberJson, SysCacheConstant.LOGIN_MEMBER_TIMEOUT);
            LoginResponseVo vo = new LoginResponseVo();
            BeanUtils.copyProperties(member, vo);
            vo.setAccessToken(token);
            return new CommonResult().success(vo);
        }
    }


    @RequestMapping("login")
    public String login(@RequestParam(value = "redirect_url") String redirect_url,
                        @CookieValue(value = "sso_user",required = false) String ssoUser,
                        HttpServletResponse response,
                        Model model
                        ) throws IOException {
        //1. 判断之前是否登录过
        if(!StringUtils.isEmpty(ssoUser)){
            //登录过,回到之前的地方并且把当前ssoserver获取到的cookie以url方式传递给其他域名【cookie同步】
            //String url = redirect_url+"?sso_user="+ssoUser;
            String url = redirect_url+"?"+"sso_user="+ssoUser;
            response.sendRedirect(url);
            return null;
        }else {
            //没有登录过
            model.addAttribute("redirect_url", redirect_url);
            return "login";
        }

    }



    @RequestMapping("/doLogin")
    public String doLogin(String username,String password,
                          String redirect_url,
                          HttpServletResponse response) throws IOException {

        //1. 模拟用户的信息
        Map<String, Object> map = new HashMap<>();
        map.put("username", username);
        map.put("email", username+"@qq.com");

        //2. 以上标识用户已登录
        String token = UUID.randomUUID().toString().replace("-","" );
        String jsonString = JSON.toJSONString(map);
        stringRedisTemplate.opsForValue().set(token,jsonString);

        //3. 登录成功做两件事：
        //①. 命令浏览器把当前的token保存为cookie： sso_user=token

        Cookie cookie = new Cookie("sso_user", token);
        response.addCookie(cookie);

        //②. 命令浏览器重定向到他之前的位置
        response.sendRedirect(redirect_url+"?sso_user="+token);

        return null;
    }
}























