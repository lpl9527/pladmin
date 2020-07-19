package com.lpl.modules.security.security;

import com.lpl.modules.security.config.SecurityProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * @author lpl
 * token提供者类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {

    private final SecurityProperties securityProperties;

    /**
     * 继承InitializingBean接口的Bean，在初始化时都会执行该方法
     */
    @Override
    public void afterPropertiesSet() throws Exception {

    }
}
