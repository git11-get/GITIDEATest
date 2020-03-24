package com.atguigu.gmall.client1.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "sso.server")
@Data
public class SsoConfig {


    //sso.server.url=http://www.ssoserver.com:8082
    //sso.server.loginpath=/login

    private String url;
    private String loginpath;



}
