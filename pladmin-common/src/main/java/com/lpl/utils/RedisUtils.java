package com.lpl.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * @author lpl
 * redis操作工具类
 */
public class RedisUtils {

    private RedisTemplate<Object, Object> redisTemplate;

    @Value("${jwt.online-key}")
    private String onlineKey;
}
