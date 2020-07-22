package com.lpl.modules.security.service;

import com.lpl.modules.security.config.SecurityProperties;
import com.lpl.modules.security.service.dto.OnlineUserDto;
import com.lpl.utils.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author lpl
 * 在线用户业务类
 */
@Slf4j
@Service
public class OnlineUserService {

    private final SecurityProperties securityProperties;

    private final RedisUtils redisUtils;

    public OnlineUserService(SecurityProperties securityProperties, RedisUtils redisUtils) {
        this.securityProperties = securityProperties;
        this.redisUtils = redisUtils;
    }

    /**
     * 从缓存中查询用户
     * @param key   在线用户对应的key
     */
    public OnlineUserDto getOne(String key) {
        return (OnlineUserDto) redisUtils.get(key);
    }
}
