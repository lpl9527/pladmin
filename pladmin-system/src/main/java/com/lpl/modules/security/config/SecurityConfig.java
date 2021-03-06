package com.lpl.modules.security.config;

import com.lpl.annotation.AnonymousAccess;
import com.lpl.modules.security.security.JwtAccessDeniedHandler;
import com.lpl.modules.security.security.JwtAuthenticationEntryPoint;
import com.lpl.modules.security.security.TokenConfigurer;
import com.lpl.modules.security.security.TokenProvider;
import com.lpl.modules.security.service.OnlineUserService;
import com.lpl.modules.security.service.UserCacheClean;
import com.lpl.utils.enums.RequestMethodEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;

/**
 * @author lpl
 * springSecurity相关配置
 */
@Configuration
@RequiredArgsConstructor
@EnableWebSecurity  //开启SpringSecurity功能，激活WebSecurityConfiguration配置类，注入了SpringSecuityFilterChain核心过滤器链
/**
 * spring security默认是关闭注解的，想要开启注解，需要在继承WebSecurityConfigurerAdapter的类上加上@EnableGlobalMethodSecurity注解，用以判断用户对某个控制层的方法是否有控制权限。
 *      1. securedEnabled=true 开启@Secured 注解过滤权限
 *      2. jsr250Enabled=true 开启@RolesAllowed 注解过滤权限
 *      3. prePostEnabled=true 使用表达式实现方法级别的安全性控制 。4个注解可用：
 *          @PreAuthorize 在方法调用之前,基于表达式的计算结果来限制对方法的访问
 *          @PostAuthorize 允许方法调用,但是如果表达式计算结果为false,将抛出一个安全性异常
 *          @PostFilter 允许方法调用,但必须按照表达式来过滤方法的结果
 *          @PreFilter 允许方法调用,但必须在进入方法之前过滤输入值
 */
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final ApplicationContext applicationContext;    //spring应用上下文
    private final CorsFilter corsFilter;    //跨域请求过滤器
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;  //自定义认证异常处理入口
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;    //自定义认证拒绝处理器
    private final TokenProvider tokenProvider;      //jwt token提供者类

    private final SecurityProperties securityProperties;    //安全配置
    private final OnlineUserService onlineUserService;
    private final UserCacheClean userCacheClean;

    /**
     * 重写实体类，去掉spring security中自动添加的ROLE_前缀，否则角色不带ROLE_前缀的角色将不会被@PreAuthorize注解识别
     */
    @Bean
    GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
    /**
     * 设置spring security默认的密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    /**
     * token安全配置适配器
     */
    private TokenConfigurer securityConfigurerAdapter() {
        return new TokenConfigurer(tokenProvider, securityProperties, onlineUserService, userCacheClean);
    }

    /**
     * spring security安全配置
     * @param httpSecurity
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity httpSecurity) throws Exception {

        //搜寻匿名标记url，即@AnonymousAccess注解标识的控制层方法对应的映射url

        //从spring上下文中获取spring mvc请求处理器映射器并从其中获取所有的处理器方法的集合
        Map<RequestMappingInfo, HandlerMethod> handlerMethodMap = applicationContext.getBean(RequestMappingHandlerMapping.class).getHandlerMethods();
        //存储匿名标记的注解标记的控制层方法对应的API url集合
        Map<String, Set<String>> anonymousUrls = getAnonymousUrl(handlerMethodMap);

        //System.err.println("可匿名访问的接口：=================" + anonymousUrls);
        httpSecurity
                //禁用CSRF（跨站请求伪造）
                .csrf().disable()
                .addFilterBefore(corsFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling()    //授权异常
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)     //自定义异常处理入口
                .accessDeniedHandler(jwtAccessDeniedHandler)        //自定义认证拒绝处理器

                //防止iframe造成跨域
                .and()
                .headers()
                .frameOptions()
                .disable()

                //不创建会话
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and()
                .authorizeRequests()
                .antMatchers(   //静态资源全部放行
                        HttpMethod.GET,
                        "/*.html",
                        "/**/*.html",
                        "/**/*.css",
                        "/**/*.js",
                        "/webSocket/**"
                ).permitAll()
                //swagger相关放行
                .antMatchers("/swagger-ui.html").permitAll()
                .antMatchers("/swagger-resources/**").permitAll()
                .antMatchers("/webjars/**").permitAll()
                .antMatchers("/*/api-docs").permitAll()
                //文件相关
                .antMatchers("/avatar/**").permitAll()
                .antMatchers("/file/**").permitAll()
                //阿里巴巴 druid
                .antMatchers("/druid/**").permitAll()
                //放心options跨域预检请求
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                //自定义匿名访问的所有url放行：允许匿名和带Token访问，粒度化到每个 Request 类型
                //Get方式
                .antMatchers(HttpMethod.GET, anonymousUrls.get(RequestMethodEnum.GET.getType()).toArray(new String[0])).permitAll()
                //Post方式
                .antMatchers(HttpMethod.POST, anonymousUrls.get(RequestMethodEnum.POST.getType()).toArray(new String[0])).permitAll()
                //Put方式
                .antMatchers(HttpMethod.PUT, anonymousUrls.get(RequestMethodEnum.PUT.getType()).toArray(new String[0])).permitAll()
                //Patch方式
                .antMatchers(HttpMethod.PATCH, anonymousUrls.get(RequestMethodEnum.PATCH.getType()).toArray(new String[0])).permitAll()
                //Delete方式
                .antMatchers(HttpMethod.DELETE, anonymousUrls.get(RequestMethodEnum.DELETE.getType()).toArray(new String[0])).permitAll()
                //所有接口类型都放行
                .antMatchers(anonymousUrls.get(RequestMethodEnum.ALL.getType()).toArray(new String[0])).permitAll()

                //上面的permitAll()方法只能对SpringSecurity定义的过滤器放行，自定义的不会放行，除非执行filterChain.doFilter(servletRequest, servletResponse)对请求进行放行。

                //除上面放行的所有请求都需要认证
                .anyRequest().authenticated()
                .and()
                .apply(securityConfigurerAdapter());    //这里的认证是使用jwt token优先，没有token时进行用户名、密码验证
    }

    /**
     * 获取@AnonymousAccess注解标注的匿名访问的接口地址集合
     * @param handlerMethodMap
     */
    private Map<String, Set<String>> getAnonymousUrl(Map<RequestMappingInfo, HandlerMethod> handlerMethodMap) {
        //存放能匿名访问的url集合，分为6种请求类型存放
        Map<String, Set<String>> anonymousUrls = new HashMap<>(6);

        Set<String> get = new HashSet<>();
        Set<String> post = new HashSet<>();
        Set<String> put = new HashSet<>();
        Set<String> delete = new HashSet<>();
        Set<String> patch = new HashSet<>();
        Set<String> all = new HashSet<>();
        //遍历将不同url存放到不同的请求类型集合中
        for (Map.Entry<RequestMappingInfo, HandlerMethod> infoEntry : handlerMethodMap.entrySet()) {
            //获取处理器方法
            HandlerMethod handlerMethod = infoEntry.getValue();
            //获取方法上的匿名访问注解
            AnonymousAccess anonymousAccess = handlerMethod.getMethodAnnotation(AnonymousAccess.class);
            if (null != anonymousAccess) {
                //获取此请求映射信息的请求方法列表
                List<RequestMethod> requestMethods = new ArrayList<>(infoEntry.getKey().getMethodsCondition().getMethods());
                //根据请求方法类型查找对应类型的枚举对象
                RequestMethodEnum requestMethodEnum = RequestMethodEnum.find(requestMethods.size() == 0 ? RequestMethodEnum.ALL.getType() : requestMethods.get(0).name());

                //匹配对应的枚举，将接口url集合添加到对应请求类型中
                switch (Objects.requireNonNull(requestMethodEnum)) {
                    case GET:
                        get.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
                        break;
                    case POST:
                        post.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
                        break;
                    case PUT:
                        put.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
                        break;
                    case PATCH:
                        patch.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
                        break;
                    case DELETE:
                        delete.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
                        break;
                    default:
                        all.addAll(infoEntry.getKey().getPatternsCondition().getPatterns());
                        break;
                }
            }
        }
        //将集合添加到Map中
        anonymousUrls.put(RequestMethodEnum.GET.getType(), get);
        anonymousUrls.put(RequestMethodEnum.POST.getType(), post);
        anonymousUrls.put(RequestMethodEnum.PUT.getType(), put);
        anonymousUrls.put(RequestMethodEnum.PATCH.getType(), patch);
        anonymousUrls.put(RequestMethodEnum.DELETE.getType(), delete);
        anonymousUrls.put(RequestMethodEnum.ALL.getType(), all);

        return anonymousUrls;
    }
}
