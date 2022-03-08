package com.tuyong.blog.service;

import com.tuyong.blog.dao.pojo.SysUser;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.params.LoginParam;

public interface LoginService {

    Result login(LoginParam loginParam);

    Result logout(String token);

    Result register(LoginParam loginParam);

    SysUser checkToken(String token);
}
