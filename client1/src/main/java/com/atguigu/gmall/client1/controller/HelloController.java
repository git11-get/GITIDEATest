package com.atguigu.gmall.client1.controller;


import com.atguigu.gmall.client1.config.SsoConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class HelloController {


    @Autowired
    private SsoConfig ssoConfig;

    /**
     * 受保护：
     * 1. sso服务器登录成功了会在url后面给我们带一个cookie
     * @param model
     * @param ssoUserCookie
     * @param request
     * @return
     */
    @RequestMapping("/")
    public String index(Model model,
                        @CookieValue(value = "sso_user",required = false) String ssoUserCookie,
                        @RequestParam(value = "sso_user",required = false) String ssoUserParam,
                        HttpServletRequest request,
                        HttpServletResponse response
                        ){

        if(!StringUtils.isEmpty(ssoUserParam)){
            //没有调用认证服务器登录后跳转回来，说明远程登录了
            Cookie sso_user = new Cookie("sso_user", ssoUserParam);
            response.addCookie(sso_user);

        }

        StringBuffer requestURL = request.getRequestURL();

        if(StringUtils.isEmpty(ssoUserCookie)){
            //没有登录

            return "redirect:"+ssoConfig.getUrl()+ssoConfig.getLoginpath()+"?redirect_url="+requestURL.toString();
        }else {
            //登录了
            model.addAttribute("loginUser", "张三");
            return "index";
        }


    }
}
