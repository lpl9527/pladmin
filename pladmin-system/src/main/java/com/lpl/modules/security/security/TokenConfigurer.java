package com.lpl.modules.security.security;

import com.lpl.modules.security.config.SecurityProperties;
import com.lpl.modules.security.service.OnlineUserService;
import com.lpl.modules.security.service.UserCacheClean;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author lpl
 * token认证配置
 */
@RequiredArgsConstructor
public class TokenConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

    private final TokenProvider tokenProvider;  //通过构造器传入的token提供者对象
    private final SecurityProperties securityProperties;
    private final OnlineUserService onlineUserService;
    private final UserCacheClean userCacheClean;

    @Override
    public void configure(HttpSecurity httpSecurity) {
        TokenFilter tokenFilter = new TokenFilter(tokenProvider, securityProperties, onlineUserService, userCacheClean);
        //在请求到达时先验证是否存在token，不存在再进行密码验证
        httpSecurity.addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
