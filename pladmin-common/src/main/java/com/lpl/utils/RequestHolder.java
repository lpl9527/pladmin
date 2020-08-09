package com.lpl.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * @author lpl
 * 请求构造工具类
 */
public class RequestHolder {

    /**
     * 获取HttpServletRequest对象
     */
    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes)Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }
}
