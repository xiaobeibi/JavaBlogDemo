package com.tuyong.blog.controller;

import com.tuyong.blog.dao.pojo.SysUser;
import com.tuyong.blog.utils.UserThreadLocal;
import com.tuyong.blog.vo.Result;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("test")
public class TestController {

    @RequestMapping
    public Result test() {
        SysUser sysUser = UserThreadLocal.get();
        System.out.println(sysUser);
        return Result.success(null);
    }
}
