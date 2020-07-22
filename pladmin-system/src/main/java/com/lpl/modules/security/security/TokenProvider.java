package com.lpl.modules.security.security;

import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lpl.modules.security.config.SecurityProperties;
import com.lpl.utils.RedisUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author lpl
 * token提供者类
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TokenProvider implements InitializingBean {

    /**
     * 基于Jwt的身份认证方案：
     *  1.客户端发送带有用户名、密码的post请求；
     *  2.服务端验证通过后生成jwt，并将用户信息放入jwt；
     *  3.将jwt存入到Cookie中；
     *  4.客户端发送带有jwt的请求；
     *  5.服务端验证签名后从jwt中获取用户信息；
     *  6.返回相应结果。
     */
    private final SecurityProperties securityProperties;    //安全相关属性配置
    private Key key;    //spring security的密钥key
    private static final String AUTHORITIES_KEY = "auth";   //认证的key
    private final RedisUtils redisUtils;    //redis工具类

    /**
     * 继承InitializingBean接口的Bean，在初始化时都会执行该方法。初始化Bean时生成token
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        //解码Base64字符串为字节数组
        byte[] keyBytes = Decoders.BASE64.decode(securityProperties.getBase64Secret());
        //根据HMAC非对称算法生成密钥
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 生成jwt，并将用户信息放入
     * @param authentication    //用户信息存储对象
     * @return  String类型jwt token
     */
    public String createToken(Authentication authentication) {  //Authentication对象用于描述当前用户的相关信息，存在于SecurityContext，会自动被创建
        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(","));

        //构建Jwt对象生成jwt token
        return Jwts.builder()   //构建Jwt对象
                .setSubject(authentication.getName())   //设置主体名称为用户名
                .claim(AUTHORITIES_KEY, authorities)
                .signWith(key, SignatureAlgorithm.HS512)    //登录采用的签名算法
                .setId(IdUtil.simpleUUID())     //加入id确保生成的token都不一样
                .compact();
    }

    /**
     * 根据jwt token获取用户名、密码认证token信息对象
     * @param token
     * @return  spring security的用户信息认证对象
     */
    Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()    //Jwt token的相关信息全都放在Claims对象中
            .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Object authoritiesStr = claims.get(AUTHORITIES_KEY);
        Collection<? extends GrantedAuthority> authorities = ObjectUtil.isNotEmpty(authoritiesStr) ?
                Arrays.stream(authoritiesStr.toString().split(",")).map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                : Collections.emptyList();
        User principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 续期检查，如果在续期时间范围内，则续期缓存过期时间
     * @param token
     */
    public void checkRenewal(String token) {
        //从缓存中获取token的过期时间，转化为毫秒
        long time = redisUtils.getExpire(securityProperties.getOnlineKey() + token) * 1000;
        //在当前日期的基础上偏移这些毫秒数得到过期日期
        Date expireDate = DateUtil.offset(new Date(), DateField.MILLISECOND, (int)time);
        //计算当前日期与过期时间的毫秒差
        long differ = expireDate.getTime() - System.currentTimeMillis();
        //如果在续期检查的范围内则续期
        if (differ < securityProperties.getDetect()){
            long renew = time + securityProperties.getRenew();  //增加延期时间的毫秒数
            redisUtils.expire(securityProperties.getOnlineKey() + token, renew, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * 从HttpServletRequest对象中获取token
     * @param request   请求对象
     * @return  token
     */
    public String getToken(HttpServletRequest request) {
        final String bearerToken = request.getHeader(securityProperties.getHeader());    //获取请求头中"Authorization"的值
        if (null != bearerToken && bearerToken.startsWith(securityProperties.getTokenStartWith())){     //如果此参数值是以Bearer 开头，就截取出token
            //去掉令牌前缀
            return bearerToken.substring(7);
        }
        return null;
    }
}
