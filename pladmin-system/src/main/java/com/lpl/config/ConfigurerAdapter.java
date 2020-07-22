package com.lpl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lpl
 * springMvc配置适配器
 */
@Configuration
@EnableWebMvc   //用于快捷配置webMvc，不需继承WebMvcConfigurationSupport，而只需要实现WebMvcConfigurer配置接口
public class ConfigurerAdapter implements WebMvcConfigurer {

    /**
     * corsFilter用于进行跨域配置。该适配方法用于解决CorsFilter与spring security的冲突，放行所有的跨域请求
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);   //允许所有证书
        config.addAllowedOrigin("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
