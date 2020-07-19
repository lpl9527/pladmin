package com.lpl.modules.security.rest;

import com.lpl.annotation.AnonymousAccess;
import com.lpl.modules.security.config.SecurityProperties;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 *  @author lpl
 *  提供授权，根据token获取用户详细信息
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
public class AuthorizationController {

    private final SecurityProperties securityProperties;

    /**
     * 获取图片验证码
     * @return
     */
    @AnonymousAccess    //可以匿名访问的方法
    @ApiOperation("获取图片验证码")
    @GetMapping("/auth/code")
    public ResponseEntity<Object> getCode() {

        //算术类型验证码 https://gitee.com/whvse/EasyCaptcha
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        //几位数运算，默认两位
        captcha.setLen(2);
        //获取运算的结果
        String result = captcha.text();
        //String uuid = securityProperties.getCodeKey() +
        //保存验证码结果到redis设置过期时间
        //TODO
        //返回验证码信息
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
            put("img", captcha.toBase64());
            //put("uuid", uuid);
        }};
        return ResponseEntity.ok(imgResult);
    }
}
