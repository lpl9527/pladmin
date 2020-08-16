package com.lpl.modules.security.service;

import com.lpl.modules.security.config.SecurityProperties;
import com.lpl.modules.security.service.dto.JwtUserDto;
import com.lpl.modules.security.service.dto.OnlineUserDto;
import com.lpl.utils.EncryptUtils;
import com.lpl.utils.RedisUtils;
import com.lpl.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
     * 从缓存中查询一个用户
     * @param key   在线用户对应的key
     */
    public OnlineUserDto getOne(String key) {
        return (OnlineUserDto) redisUtils.get(key);
    }

    /**
     * 根据过滤条件从缓存中查找在线用户列表
     * @param filter 过滤条件
     */
    public List<OnlineUserDto> getAll(String filter) {
        //从redis中模糊匹配查找所有的在线用户的key列表
        List<String> keys = redisUtils.scan(securityProperties.getOnlineKey() + "*");
        //集合反转
        Collections.reverse(keys);
        List<OnlineUserDto> onlineUserDtos = new ArrayList<>();
        //遍历，从中找出指定用户名的在线用户并放入在线用户列表
        for (String key : keys) {
            //根据key从redis中查询在线用户信息
            OnlineUserDto onlineUserDto = (OnlineUserDto) redisUtils.get(key);
            if (StringUtils.isNotBlank(filter)){
                if (onlineUserDto.toString().contains(filter)){ //符合条件的加入到列表
                    onlineUserDtos.add(onlineUserDto);
                }
            }else { //没有过滤条件时直接加到列表
                onlineUserDtos.add(onlineUserDto);
            }
        }
        //根据登录时间排序
        onlineUserDtos.sort(((o1, o2) -> o2.getLoginTime().compareTo(o1.getLoginTime())));
        return onlineUserDtos;
    }

    /**
     * 保存在线用户
     * @param jwtUserDto
     * @param token
     * @param request
     */
    public void save(JwtUserDto jwtUserDto, String token, HttpServletRequest request) {
        //获取部门名称
        String deptName = jwtUserDto.getUser().getDept().getName();
        //获取请求ip
        String ip = StringUtils.getIp(request);
        //获取浏览器名称
        String browser = StringUtils.getBrowser(request);
        //获取ip对应地址
        String address = StringUtils.getAddressInfo(ip);

        OnlineUserDto onlineUserDto = null;
        try {
            //构造在线用户数据传输对象，其中对token进行了DES对称加密
            onlineUserDto = new OnlineUserDto(jwtUserDto.getUsername(), jwtUserDto.getUser().getNickName(), deptName, browser, ip, address, EncryptUtils.desEncrypt(token), new Date());
        }catch (Exception e) {
            e.printStackTrace();
        }
        //将在线用户信息放入redis
        redisUtils.set(securityProperties.getOnlineKey() + token, onlineUserDto, securityProperties.getTokenValidityInMillisecond()/1000);
    }

    /**
     * 检测用户是否在之前已经登录，已经登录的踢下线
     * @param username  用户名称
     * @param ignoreToken  忽略的token（当前新登录的token不进行剔除）
     */
    public void checkLoginOnUser(String username, String ignoreToken) {
        //获取指定用户名称的所有在线用户
        List<OnlineUserDto> onlineUserDtos = getAll(username);
        if (null == onlineUserDtos || onlineUserDtos.isEmpty()) {
            return;
        }
        for(OnlineUserDto onlineUserDto : onlineUserDtos) {
            if (onlineUserDto.getUserName().equals(username)) {
                try {
                    //DES对称解密token
                    String token = EncryptUtils.desDecrypt(onlineUserDto.getKey());
                    if (StringUtils.isNotBlank(ignoreToken) && !ignoreToken.equals(token)) {
                        //要剔除的用户不是当前用户时踢除
                        this.kickOut(token);
                    }else if (StringUtils.isBlank(ignoreToken)) {
                        this.kickOut(token);
                    }
                }catch (Exception e) {
                    log.error("检查用户错误！", e);
                }
            }
        }
    }

    /**
     * 踢除在线用户
     * @param key 在线用户token值
     */
    public void kickOut(String key) {
        key = securityProperties.getOnlineKey() + key;
        redisUtils.del(key);
    }

    /**
     * 用户登出
     * @param token
     */
    public void logout(String token) {
        String key = securityProperties.getOnlineKey() + token;
        redisUtils.del(key);
    }

    /**
     * 根据用户名强退用户
     * @param username
     */
    @Async
    public void kickOutForUsername(String username) {
        List<OnlineUserDto> onlineUsers = getAll(username);
        for (OnlineUserDto onlineUser : onlineUsers) {
            if (onlineUser.getUserName().equals(username)) {
                kickOut(onlineUser.getKey());
            }
        }
    }
}
