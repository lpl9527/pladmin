package com.lpl.modules.security.service;

import com.lpl.utils.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author lpl
 * 用户缓存清理工具类
 */
@Component
public class UserCacheClean {

    /**
     * 用于用户信息变更时清理用户缓存信息
     * @param username
     */
    public void cleanUserCache(String username) {
        if (StringUtils.isNotEmpty(username)) {
            //获取用户缓存信息并根据当前登录用户名清理掉用户缓存信息
            UserDetailsServiceImpl.userDtoCache.remove(username);
        }
    }

    /**
     * 清理掉所有的用户缓存信息
     */
    public void cleanAll() {
        UserDetailsServiceImpl.userDtoCache.clear();
    }
}
