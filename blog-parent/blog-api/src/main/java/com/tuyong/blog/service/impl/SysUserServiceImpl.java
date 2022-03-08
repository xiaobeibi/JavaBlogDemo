package com.tuyong.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tuyong.blog.dao.mapper.SysUserMapper;
import com.tuyong.blog.dao.pojo.SysUser;
import com.tuyong.blog.service.LoginService;
import com.tuyong.blog.service.SysUserService;
import com.tuyong.blog.utils.JWTUtils;
import com.tuyong.blog.vo.ErrorCode;
import com.tuyong.blog.vo.LoginUserVo;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.UserVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SysUserServiceImpl implements SysUserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public SysUser findSysUserById(Long userId) {
        SysUser sysUser = sysUserMapper.selectById(userId);
        if (sysUser == null) {
            sysUser = new SysUser();
            sysUser.setNickname("小贝比");
        }
        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String pwd) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount, account);
        queryWrapper.eq(SysUser::getPassword, pwd);
        queryWrapper.select(SysUser::getId, SysUser::getAccount, SysUser::getAvatar, SysUser::getNickname);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public Result getUserInfoByToken(String token) {
        Map<String, Object> map = JWTUtils.checkToken(token);
        if (map == null) {
            return Result.fail(ErrorCode.NO_LOGIN.getCode(), ErrorCode.NO_LOGIN.getMsg());
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (StringUtils.isBlank(userJson)) {
            return Result.fail(ErrorCode.NO_LOGIN.getCode(), ErrorCode.NO_LOGIN.getMsg());
        }
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setAccount(sysUser.getAccount());
        loginUserVo.setAvatar(sysUser.getAvatar());
        loginUserVo.setId(String.valueOf(sysUser.getId()));
        loginUserVo.setNickname(sysUser.getNickname());
        return Result.success(loginUserVo);
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount, account);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        //注意 默认生成的id 是分布式id 采用了雪花算法
        sysUserMapper.insert(sysUser);
    }

    @Override
    public UserVo findUserVoById(Long authorId) {
        SysUser sysUser = sysUserMapper.selectById(authorId);
        if (sysUser == null) {
            sysUser = new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/user/admin.png");
            sysUser.setNickname("小贝比");
        }
        UserVo userVo = new UserVo();
        userVo.setAvatar(sysUser.getAvatar());
        userVo.setNickname(sysUser.getNickname());
        userVo.setId(String.valueOf(sysUser.getId()));
        return userVo;
    }
}
