package com.lpl.utils;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.lpl.exception.BadRequestException;
import com.lpl.utils.enums.DataScopeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

/**
 * @author lpl
 * 认证相关工具类
 */
@Slf4j
public class SecurityUtils {

    /**
     * 获取当前登录的用户（UserDetails对象）
     * @return UserDetails对象
     */
    public static UserDetails getCurrentUser() {
        //从Security上下文中获取认证对象
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication) {
            throw new BadRequestException(HttpStatus.UNAUTHORIZED, "登录状态过期，请重新登录！");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails)authentication.getPrincipal();   //从当前认证对象中获取用户信息

            //获取UserDetailsService对象
            UserDetailsService userDetailsService = SpringContextHolder.getBean(UserDetailsService.class);
            //执行UserDetailsService的loadUserByUsername(username)方法根据当前认证对象中用户名重新获取UserDetails对象，保证认证对象认证状态正确
            return userDetailsService.loadUserByUsername(userDetails.getUsername());
        }
        throw new BadRequestException(HttpStatus.UNAUTHORIZED, "找不到当前登录信息！");
    }
    /**
     * 获得当前登录用户名称
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (null == authentication) {
            throw new BadRequestException(HttpStatus.UNAUTHORIZED, "登录状态过期，请重新登录！");
        }
        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails)authentication.getPrincipal();
            return userDetails.getUsername();
        }
        throw new BadRequestException(HttpStatus.UNAUTHORIZED, "找不到当前登录信息！");
    }
    /**
     * 获取当用户的Id
     */
    public static Long getCurrentUserId() {
        UserDetails userDetails = getCurrentUser();
        Object user = new JSONObject(userDetails).get("user");
        return new JSONObject(user).get("id", Long.class);
    }
    /**
     * 获取当前用户的数据权限
     */
    public static List<Long> getCurrentUserDataScope() {
        UserDetails userDetails = getCurrentUser();
        JSONArray jsonArray = JSONUtil.parseArray(new JSONObject(userDetails).get("dataScopes"));
        return JSONUtil.toList(jsonArray, Long.class);
    }

    /**
     * 获取数据权限级别
     * @return 级别
     */
    public static String getDataScopeType() {
        List<Long> dataScopes = getCurrentUserDataScope();
        if(dataScopes.size() != 0){
            return "";
        }
        return DataScopeEnum.ALL.getValue();
    }
}
