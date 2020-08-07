package com.lpl.modules.system.service.impl;

import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.extra.template.Template;
import cn.hutool.extra.template.TemplateConfig;
import cn.hutool.extra.template.TemplateEngine;
import cn.hutool.extra.template.TemplateUtil;
import com.lpl.domain.vo.EmailVo;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.service.VerifyService;
import com.lpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class verifyServiceImpl implements VerifyService {

    @Value("${code.expiration}")
    private Long expiration;    //修改邮箱、密码验证码过期时间

    private final RedisUtils redisUtils;

    /**
     * 构建邮件验证码对象
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EmailVo BuildEmailCodeVO(String email, String key) {
        EmailVo emailVo;
        String content;
        String redisKey = key + email;  //验证码分类键加上邮箱作为新的key

        //获取邮件模板
        //从classpath下查找template路径作为模板引擎的根目录
        TemplateConfig templateConfig = new TemplateConfig("template", TemplateConfig.ResourceMode.CLASSPATH);
        TemplateEngine engine = TemplateUtil.createEngine(templateConfig);
        //使用模板引擎获取模板
        Template template = engine.getTemplate("email/email.ftl");

        //从redis中查找此邮箱是否已经发送过验证码
        Object oldCode = redisUtils.get(redisKey);
        //当当前邮箱的验证码为空时才进行发送
        if (null == oldCode) {
            //生成6位随机验证码
            String code = RandomUtil.randomNumbers(6);
            //存入redis，并设置过期时间
            if (!redisUtils.set(redisKey, code, expiration)) {
                throw new BadRequestException("服务异常，请联系管理员！");
            }
            //获取模板内容
            content = template.render(Dict.create().set("code", code));
            //创建视图对象
            emailVo = new EmailVo(Collections.singletonList(email), "PL-ADMIN后天管理系统", content);
        }else {     //已经发送过验证码就再次发送原来的验证码
            content = template.render(Dict.create().set("code", oldCode));
            emailVo = new EmailVo(Collections.singletonList(email), "PL-ADMIN后台管理系统", content);
        }

        return emailVo;
    }
}
