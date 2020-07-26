package com.lpl.modules.security.service.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author lpl
 * 用于登录认证的用户数据传输对象
 */
@Getter
@Setter
public class AuthUserDto {

    @NotBlank
    private String username;    //用户名

    @NotBlank
    private String password;    //密码

    private String code;    //验证码答案

    private String uuid = "";   //验证码在redis中的key

    @Override
    public String toString() {
        return "{username=" + username  + ", password= ******}";
    }
}
