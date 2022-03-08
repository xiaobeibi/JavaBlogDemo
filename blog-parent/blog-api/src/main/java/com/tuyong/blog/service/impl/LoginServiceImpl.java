package com.tuyong.blog.service.impl;

import com.alibaba.fastjson.JSON;
import com.tuyong.blog.dao.pojo.SysUser;
import com.tuyong.blog.service.LoginService;
import com.tuyong.blog.service.SysUserService;
import com.tuyong.blog.utils.JWTUtils;
import com.tuyong.blog.vo.ErrorCode;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.params.LoginParam;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
public class LoginServiceImpl implements LoginService {

    private static final String slat = "TuYong!@#";

    @Autowired
    private SysUserService sysUserService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public Result login(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        String pwd = DigestUtils.md5Hex(password + slat);
        SysUser sysUser = sysUserService.findUser(account, pwd);
        if (sysUser == null) {
            return Result.fail(ErrorCode.ACCOUNT_PWD_NOT_EXIST.getCode(), ErrorCode.ACCOUNT_PWD_NOT_EXIST.getMsg());
        }
        //登录成功，使用JWT生成token，返回token和redis中
        String token = JWTUtils.createToken(sysUser.getId());
        redisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(sysUser), 1, TimeUnit.DAYS);
        return Result.success(token);
    }

    @Override
    public Result logout(String token) {
        redisTemplate.delete("TOKEN_" + token);
        return Result.success(null);
    }

    @Override
    public Result register(LoginParam loginParam) {
        String account = loginParam.getAccount();
        String password = loginParam.getPassword();
        String nickname = loginParam.getNickname();
        if (StringUtils.isBlank(account) || StringUtils.isBlank(password) || StringUtils.isBlank(nickname)) {
            return Result.fail(ErrorCode.PARAMS_ERROR.getCode(), ErrorCode.PARAMS_ERROR.getMsg());
        }
        SysUser sysUser = sysUserService.findUserByAccount(account);
        if (sysUser != null) {
            return Result.fail(ErrorCode.ACCOUNT_EXIST.getCode(), ErrorCode.ACCOUNT_EXIST.getMsg());
        }
        sysUser = new SysUser();
        sysUser.setNickname(nickname);
        sysUser.setAccount(account);
        sysUser.setPassword(DigestUtils.md5Hex(password + slat));
        sysUser.setCreateDate(System.currentTimeMillis());
        sysUser.setLastLogin(System.currentTimeMillis());
        sysUser.setAvatar("/static/img/logo.b3a48c0.png");
        sysUser.setAdmin(1); //1 为true
        sysUser.setDeleted(0); // 0 为false
        sysUser.setSalt(slat);
        sysUser.setStatus("");
        sysUser.setEmail("");
        sysUserService.save(sysUser);
        //token
        String token = JWTUtils.createToken(sysUser.getId());

        redisTemplate.opsForValue().set("TOKEN_" + token, JSON.toJSONString(sysUser), 1, TimeUnit.DAYS);
        return Result.success(token);
    }

    @Override
    public SysUser checkToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        Map<String, Object> stringObjectMap = JWTUtils.checkToken(token);
        if (stringObjectMap == null) {
            return null;
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (StringUtils.isBlank(userJson)) {
            return null;
        }
        return JSON.parseObject(userJson, SysUser.class);
    }
}
