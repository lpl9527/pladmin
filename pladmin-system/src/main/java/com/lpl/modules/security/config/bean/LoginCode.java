package com.lpl.modules.security.config.bean;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author lpl
 * 登录验证码相关配置
 */
@Data
public class LoginCode {

    @Value("${login.login-code.code-type}")
    private LoginCodeEnum codeType;     //验证码类型

    @Value("${login.login-code.expiration}")
    private Long expiration = 2L;       //验证码有效期（单位：分钟）

    @Value("${login.login-code.length}")
    private int length = 2;             //验证码内容长度

    @Value("${login.login-code.width}")
    private int width = 111;            //验证码宽度

    @Value("${login.login-code.height}")
    private int height = 36;            //验证码高度

    @Value("${login.login-code.font-name}")
    private String fontName;            //验证码字体

    @Value("${login.login-code.font-size}")
    private int fontSize = 25;          //内容字体大小

    public LoginCodeEnum getCodeType() {
        return codeType;
    }
}
