package com.atguigu.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class GmallCartApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallCartApplication.class, args);
    }

}
