package com.atguigu.gmall.portal.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
public class ThreadPoolController {


    @Autowired
    @Qualifier("mainThreadPoolExecutor")
    private ThreadPoolExecutor mainThreadPoolExecutor;


    @GetMapping("thread/status")
    public Map threadPoolStatue(){

        Map<String, Object> map = new HashMap<>();
        map.put("activeCount", mainThreadPoolExecutor.getActiveCount());
        map.put("corePoolSize", mainThreadPoolExecutor.getCorePoolSize());
        return map;
    }
}
