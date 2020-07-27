package com.lpl.utils;

/**
 * @author lpl
 * 存放系统常用静态常量
 */
public class AppConstant {

    /**
     * windows系统
     */
    private static final String WIN = "win";
    /**
     * mac os
     */
    private static final String MAC = "mac";

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
