package com.lpl.modules.security.config.bean;

import lombok.Data;

/**
 * @author lpl
 * 登录验证码相关配置
 */
@Data
public class LoginCode {

    private LoginCodeEnum codeType;     //验证码类型

    private Long expiration = 2L;       //验证码有效期（单位：分钟）

    private int length = 2;             //验证码内容长度

    private int width = 111;            //验证码宽度

    private int height = 36;            //验证码高度

    private String fontName;            //验证码字体

    private int fontSize = 25;          //内容字体大小

    public LoginCodeEnum getCodeType() {
        return codeType;
    }
}
