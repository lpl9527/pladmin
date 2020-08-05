package com.lpl.utils;

/**
 * @author lpl
 * 存放系统常用静态常量
 */
public class AppConstant {

    /**
     * 用于IP定位转换
     */
    public static final String REGION = "内网IP|内网IP";
    /**
     * windows系统
     */
    public static final String WIN = "win";
    /**
     * mac os
     */
    public static final String MAC = "mac";

    /**
     * 常用接口uri
     */
    public static class Url {
        //免费图床
        public static final String SM_MS_URL = "https://sm.ms/api";
        //根据ip查询所在地
        public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true";
    }
}
