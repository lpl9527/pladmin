package com.lpl.modules.system.service;

import com.lpl.domain.vo.EmailVo;

/**
 * 验证业务类
 */
public interface VerifyService {

    /**
     * 构建邮箱验证码视图对象
     * @param email 新邮箱
     * @param key redis中key
     */
    EmailVo BuildEmailCodeVO(String email, String key);

    /**
     * 验证redis中验证码
     * @param key   键
     * @param code  前台输入的验证码
     */
    void validatedCode(String key, String code);
}
