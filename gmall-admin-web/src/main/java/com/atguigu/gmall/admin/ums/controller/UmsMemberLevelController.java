package com.atguigu.gmall.admin.ums.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.to.CommonResult;
import com.atguigu.gmall.ums.entity.MemberLevel;
import com.atguigu.gmall.ums.service.MemberLevelService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@CrossOrigin
@RestController
public class UmsMemberLevelController {

    @Reference
    private MemberLevelService memberLevelService;


    @RequestMapping("memberLevel/list")
    public Object memberLevelList(){

        List<MemberLevel> list = memberLevelService.list();

        return new CommonResult().success(list);
    }


}





























