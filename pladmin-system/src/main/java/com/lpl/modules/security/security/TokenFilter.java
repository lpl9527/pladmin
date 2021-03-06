package com.lpl.modules.security.security;

import cn.hutool.core.util.StrUtil;
import com.lpl.modules.security.config.SecurityProperties;
import com.lpl.modules.security.service.OnlineUserService;
import com.lpl.modules.security.service.UserCacheClean;
import com.lpl.modules.security.service.dto.OnlineUserDto;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Objects;

/**
 * @author lpl
 * token过滤器
 */
@Slf4j
@AllArgsConstructor
public class TokenFilter extends GenericFilterBean {

    private final TokenProvider tokenProvider;  //通过构造器传入的token提供者对象
    private final SecurityProperties securityProperties;
    private final OnlineUserService onlineUserService;
    private final UserCacheClean userCacheClean;

    /**
     * 自定义过滤器，用户登录请求令牌jwt token进行处理的Filter，使用token换取认证对象信息，用于进行下一步认证
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        //获取token
        String token = tokenProvider.getToken(httpServletRequest);

        if (StrUtil.isNotBlank(token)){     //如果请求中有token，则需要去查redis进行token续期，以及添加到认证上下文
            OnlineUserDto onlineUserDto = null; //在线用户
            boolean cleanUserCache = false;     //是否清理用户信息缓存
            try{
                //从缓存中取出token对应在线用户信息
                onlineUserDto = onlineUserService.getOne(securityProperties.getOnlineKey() + token);
            }catch (Exception e) {
                log.error(e.getMessage());
                cleanUserCache = true;
            }finally {
                if (cleanUserCache || Objects.isNull(onlineUserDto)) {
                    userCacheClean.cleanUserCache(String.valueOf(tokenProvider.getClaims(token).get(TokenProvider.AUTHORITIES_KEY)));
                }
            }
            if (null != onlineUserDto && StringUtils.hasText(token)) {  //如果用户在缓存中
                //根据token获取用户名、密码认证信息对象
                Authentication authentication = tokenProvider.getAuthentication(token);
                //将认证信息对象放入SecurityContext上下文中，用于进行下一步TokenConfigurer中的UsernamePasswordAuthenticationFilter验证。
                SecurityContextHolder.getContext().setAuthentication(authentication);
                //token续期（如果在token有效期的话）
                tokenProvider.checkRenewal(token);
            }
        }
        //放行请求
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
