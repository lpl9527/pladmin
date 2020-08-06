package com.lpl.modules.system.service.impl;

import com.lpl.domain.vo.EmailVo;
import com.lpl.modules.system.service.VerifyService;

public class verifyServiceImpl implements VerifyService {

    /**
     * 发送邮件验证码
     */
    @Override
    public EmailVo sendEmailCode(String email, String key) {
        EmailVo emailVo;
        String content;
        String redisKey = key + email;

        return null;
    }
}
