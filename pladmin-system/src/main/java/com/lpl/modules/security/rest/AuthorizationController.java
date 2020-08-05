package com.lpl.modules.security.rest;

import cn.hutool.core.util.IdUtil;
import com.lpl.annotation.AnonymousAccess;
import com.lpl.config.RsaProperties;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.security.config.SecurityProperties;
import com.lpl.modules.security.security.TokenProvider;
import com.lpl.modules.security.service.OnlineUserService;
import com.lpl.modules.security.service.dto.AuthUserDto;
import com.lpl.modules.security.service.dto.JwtUserDto;
import com.lpl.utils.RedisUtils;
import com.lpl.utils.RsaUtils;
import com.lpl.utils.SecurityUtils;
import com.lpl.utils.StringUtils;
import com.wf.captcha.ArithmeticCaptcha;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *  @author lpl
 *  提供授权相关API
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
public class AuthorizationController {

    /**
     * 这里简单说一下SpringSecurity认证的流程（可以参考UsernamePasswordAuthenticationFilter类的源码）：
     *
     *  1.首先进入过滤器AbstractAuthenticationProcessingFilter，执行doFilter()方法；
     *  2.需要进行验证的请求会调用实现类UsernamePasswordAuthenticationFilter的attemptAuthentication(request, response)方法将request中传来的username、
     *      password构造UsernamePasswordAuthenticationToken对象（实际为Authentication接口的实现类）。
     *  3.通过ProviderManager类的authenticate(authentication)方法将上面的获取的UsernamePasswordAuthenticationToken对象作为参数传入进行认证。但实际
     *      上ProviderManager管理了许多AuthenticationProviders，验证工作交由AuthenticationProvider来处理，调用实现类AbstractUserDetailsAuthenticationProvider的
     *      authenticate(authentication)来处理。
     *  4.此时AbstractUserDetailsAuthenticationProvider会调用子类DaoAuthenticationProvider的retrieveUser(username, authentication)方法来进行验证，而方法中会
     *      调用UserDetailsService接口对应实现类（需要我们实现，根据用户名从数据库中查询出用户信息）的loadUserByUserName(username)方法（这个username参数就是上一步传入
     *      的待验证的用户名）获取用户信息，返回UserDetails对象。    可以参考DaoAuthenticationProvider类的源码。
     *  5.最后AbstractUserDetailsAuthenticationProvider类再根据DaoAuthenticationProvider返回的UserDetails对象与待验证的用户名、密码进行对比，然后返回Authentication对象。
     *
     *  补充：SpringSecurity通过Session来保存用户信息。
     *      可以通过SecurityContextHolder.getContext().setAuthentication(authentication)将认证信息放入Session中。
     *      可以通过SecurityContextHolder.getContext().getAuthentication()来获得认证信息。
     */

    @Value("${loginCode.expiration}")
    private Long expiration;    //登录图形验证码过期时间（单位：分钟）
    @Value("${single.login}")
    private Boolean singleLogin;

    private final SecurityProperties securityProperties;    //spring security属性配置
    private final RedisUtils redisUtils;    //redis工具类
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final TokenProvider tokenProvider;
    private final OnlineUserService onlineUserService;

    /**
     * 登录逻辑R
     * @param authUser  前端传入的认证对象信息
     * @param request
     * @throws Exception
     */
    @AnonymousAccess
    @ApiOperation("登录授权")
    @PostMapping(value = "/auth/login")
    public ResponseEntity<Object> login(@Validated @RequestBody AuthUserDto authUser, HttpServletRequest request) throws Exception{  //@Validated注解用于对传入的实体bean属性进行校验

        //密码解密
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, authUser.getPassword());
        //从redis中查询验证码
        String code = (String) redisUtils.get(authUser.getUuid());
        //拿到验证码后就从redis中清除此验证码
        redisUtils.del(authUser.getUuid());

        if (StringUtils.isBlank(code)) {
            throw new BadRequestException("验证码不存在或已过期！");
        }
        if (StringUtils.isBlank(authUser.getCode()) || !authUser.getCode().equalsIgnoreCase(code)) {
            throw new BadRequestException("验证码错误！");
        }
        //构造UsernamePasswordAuthenticationToken对象
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(authUser.getUsername(), password);
        //进行认证
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);
        //放到认证上下文中
        SecurityContextHolder.getContext().setAuthentication(authentication);
        //生成token令牌
        String token = tokenProvider.createToken(authentication);
        final JwtUserDto jwtUserDto = (JwtUserDto) authentication.getPrincipal();
        //保存在线用户信息
        onlineUserService.save(jwtUserDto, token, request);
        //返回token与用户信息（返回的token用于回调时在TokenFilter中进行验证）
        Map<String, Object> authInfo = new HashMap<String, Object>(2){{
            put("token", securityProperties.getTokenStartWith() + token);
            put("user", jwtUserDto);
        }};
        if (singleLogin) {  //如果只允许单点登录
            //踢掉之前已经存在的token
            onlineUserService.checkLoginOnUser(authUser.getUsername(), token);
        }
        return ResponseEntity.ok(authInfo);
    }

    /**
     * 获取图片验证码
     */
    @AnonymousAccess    //可以匿名访问的方法
    @ApiOperation("获取图片验证码")
    @GetMapping("/auth/code")
    public ResponseEntity<Object> getCode() {

        //算术类型验证码 https://gitee.com/whvse/EasyCaptcha
        ArithmeticCaptcha captcha = new ArithmeticCaptcha(111, 36);
        //几位数运算，默认两位
        captcha.setLen(2);
        //获取运算的结果
        String result = captcha.text();
        String uuid = securityProperties.getCodeKey() + IdUtil.simpleUUID();    //将code-key-与uuid拼接作为验证码的key
        //保存验证码结果到redis并设置过期时间
        redisUtils.set(uuid, result, expiration, TimeUnit.MINUTES);       //将图形验证码结果放入redis，key为生成的uuid
        //返回验证码信息
        Map<String, Object> imgResult = new HashMap<String, Object>(2) {{
            put("img", captcha.toBase64());     //img为svg图片验证码图案
            put("uuid", uuid);      //每张图对应一个uuid，传到前端，用于登录时再返回到服务端作为key从缓存中查找验证码结果进行比对
        }};
        return ResponseEntity.ok(imgResult);
    }

    /**
     * 获取当前用户信息
     */
    @ApiOperation("获取用户信息")
    @GetMapping("/auth/info")
    public ResponseEntity<Object> getUserInfo() {
        return ResponseEntity.ok(SecurityUtils.getCurrentUser());
    }

    /**
     * 登出
     * @param request
     */
    @AnonymousAccess
    @ApiOperation("退出登录")
    @DeleteMapping(value = "/auth/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request) {
        onlineUserService.logout(tokenProvider.getToken(request));
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
