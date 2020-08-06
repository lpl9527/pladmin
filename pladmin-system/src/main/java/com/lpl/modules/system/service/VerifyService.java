package com.lpl.modules.system.service;

import com.lpl.domain.vo.EmailVo;

/**
 * 验证业务类
 */
public interface VerifyService {

    /**
     * 发送邮箱重置验证码
     * @param email 新邮箱
     * @param key redis中key
     */
    EmailVo sendEmailCode(String email, String key);
}
