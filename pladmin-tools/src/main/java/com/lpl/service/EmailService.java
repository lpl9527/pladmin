package com.lpl.service;

import com.lpl.domain.EmailConfig;
import com.lpl.domain.vo.EmailVo;

/**
 * @author lpl
 * 邮件业务类
 */
public interface EmailService {

    /**
     * 发送邮箱验证码
     * @param emailVo   邮件视图对象
     * @param emaiLConfig   邮件配置
     */
    void send(EmailVo emailVo, EmailConfig emaiLConfig);

    /**
     * 查询管理员邮件配置
     */
    EmailConfig find();
}
