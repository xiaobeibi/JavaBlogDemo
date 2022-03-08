package com.tuyong.blog.vo.params;

import lombok.Data;

@Data
public class LoginParam {

    private String account;

    private String password;

    private String nickname;
}
