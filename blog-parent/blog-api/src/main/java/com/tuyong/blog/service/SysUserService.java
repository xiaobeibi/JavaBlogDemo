package com.tuyong.blog.service;

import com.tuyong.blog.dao.pojo.SysUser;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.UserVo;

public interface SysUserService {

    SysUser findSysUserById(Long userId);

    SysUser findUser(String account, String pwd);

    Result getUserInfoByToken(String token);

    SysUser findUserByAccount(String account);

    void save(SysUser sysUser);

    UserVo findUserVoById(Long authorId);
}
