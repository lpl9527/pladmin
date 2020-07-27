package com.lpl.modules.security.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author lpl
 * 在线用户数据访问对象，存储在redis中
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineUserDto {

    /**
     * 用户名
     */
    private String userName;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 岗位
     */
    private String dept;
    /**
     * 浏览器
     */
    private String browser;
    /**
     * IP
     */
    private String ip;
    /**
     * 地址
     */
    private String address;
    /**
     * 对token进行加密的值
     */
    private String key;
    /**
     * 登录时间
     */
    private Date loginTime;

}
