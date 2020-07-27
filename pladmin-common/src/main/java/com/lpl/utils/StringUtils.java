package com.lpl.utils;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import eu.bitwalker.useragentutils.Browser;
import eu.bitwalker.useragentutils.UserAgent;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author lpl
 * 字符串操作工具类
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    private static final String UNKNOWN = "unknown";

    /**
     * 获取请求ip
     * @param request
     */
    public static String getIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (null == ip || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (null == ip || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (null == ip || ip.length() == 0 || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        String comma = ",";
        String localhost = "127.0.0.1";
        if (ip.contains(comma)) {
            ip = ip.split(",")[0];
        }
        if (localhost.equals(ip)) { //获取本机真实ip
            try {
                ip = InetAddress.getLocalHost().getHostAddress();
            }catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
        return ip;
    }

    /**
     * 获取用户浏览器名称
     * @param request
     */
    public static String getBrowser(HttpServletRequest request) {
        //获取用户代理对象
        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader("User-Agent"));
        Browser browser = userAgent.getBrowser();
        return browser.getName();
    }

    /**
     * 根据ip获取所在地址信息
     * @param ip
     */
    public static String getAddressInfo(String ip) {
        //format()函数可以将第一个参数后面的可变参数赋值给对应的变量中，例如：
        //      http://whois.pconline.com.cn/ipJson.jsp?ip=%s&json=true     ip参数可将%s进行替换
        String api = String.format(AppConstant.Url.IP_URL, ip);
        //发送get请求获取地址信息
        JSONObject jsonObject = JSONUtil.parseObj(HttpUtil.get(api));
        return jsonObject.get("addr", String.class);
    }

    public static void main(String[] args) {
        getAddressInfo("117.71.53.38");
    }

}
