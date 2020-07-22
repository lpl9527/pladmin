package com.lpl.modules.security.security;

import com.lpl.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author lpl
 * token过滤器
 */
@Slf4j
@RequiredArgsConstructor
public class TokenFilter extends GenericFilterBean {

    private final TokenProvider tokenProvider;  //通过构造器传入的token提供者对象

    /**
     * 用户登录令牌jwt token进行验证 Filter
     */
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;
        //获取token
        String token = tokenProvider.getToken(httpServletRequest);
        //
        if (StringUtils.isNotBlank(token)){     //如果请求中没有token，则不需要去查redis进行token续期，以及添加到认证上下文

        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
