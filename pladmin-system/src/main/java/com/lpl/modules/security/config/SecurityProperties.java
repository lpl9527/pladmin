package com.lpl.modules.security.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author lpl
 * Jwt token属性参数配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "jwt")
public class SecurityProperties {

    private String header;  //请求头：Authorization

    private String tokenStartWith;  //令牌前缀（最后有个空格）：Bearer

    private String base64Secret;    //最少使用88位的base64对该令牌进行编码

    private Long tokenValidityInMillisecond;    //令牌过期时间，单位：毫秒

    private String onlineKey;   //在线用户key，根据此key查询redis中在线用户的数据

    private String codeKey;     //验证码key

    private Long detect;    //续期检查时间，单位：毫秒

    private Long renew;     //续期时间，单位：毫秒

    /**
     * 获取jwt token令牌前缀，加个空格
     */
    public String getTokenStartWith() {
        return tokenStartWith + " ";
    }
}
