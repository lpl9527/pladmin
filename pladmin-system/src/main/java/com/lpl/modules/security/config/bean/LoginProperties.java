package com.lpl.modules.security.config.bean;

import com.lpl.exception.BadConfigurationException;
import com.lpl.utils.StringUtils;
import com.wf.captcha.*;
import com.wf.captcha.base.Captcha;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Objects;

/**
 * @author lpl
 * 登录相关配置
 */
@Data
@Component
public class LoginProperties {

    @Value("${login.single}")
    private boolean singleLogin = false;    //是否单用户登录

    private LoginCode loginCode;    //登录验证码

    @Value("${login.cache-enable}")
    private boolean cacheEnable;    //是否缓存登录信息

    public boolean isSingleLogin() {
        return singleLogin;
    }

    public boolean isCacheEnable() {
        return cacheEnable;
    }

    /**
     * 获取验证码生产类
     */
    public Captcha getCaptcha() {
        if (Objects.isNull(loginCode)) {
            loginCode = new LoginCode();
            if (Objects.isNull(loginCode.getCodeType())) {
                loginCode.setCodeType(LoginCodeEnum.arithmetic);
            }
        }
        return switchCaptcha(loginCode);
    }

    /**
     * 根据配置信息生产验证码
     * @param loginCode
     */
    private Captcha switchCaptcha(LoginCode loginCode) {
        Captcha captcha;
        synchronized (this) {
            switch (loginCode.getCodeType()) {
                case arithmetic:
                    // 算术类型 https://gitee.com/whvse/EasyCaptcha
                    captcha = new ArithmeticCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    // 几位数运算，默认是两位
                    captcha.setLen(loginCode.getLength());
                    break;
                case chinese:
                    captcha = new ChineseCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
                case chinese_gif:
                    captcha = new ChineseGifCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
                case gif:
                    captcha = new GifCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
                case spec:
                    captcha = new SpecCaptcha(loginCode.getWidth(), loginCode.getHeight());
                    captcha.setLen(loginCode.getLength());
                    break;
                default:
                    throw new BadConfigurationException("验证码配置信息错误，正确配置请查看 LoginCodeEnum");
            }
        }
        if(StringUtils.isNotBlank(loginCode.getFontName())){
            captcha.setFont(new Font(loginCode.getFontName(), Font.PLAIN, loginCode.getFontSize()));
        }
        return captcha;
    }

    public static void main(String[] args) {
        //验证码测试
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        captcha.setLen(2);
        String result = captcha.text();
        String s = captcha.toBase64();
        System.out.println("验证码运算结果===" + result);
        System.out.println("验证码图案数据===" + s);
    }
}
