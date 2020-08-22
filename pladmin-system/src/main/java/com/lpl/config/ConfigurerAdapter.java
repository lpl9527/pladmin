package com.lpl.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author lpl
 * springMvc配置适配器
 */
@Configuration
@EnableWebMvc   //用于快捷配置webMvc，不需继承WebMvcConfigurationSupport，而只需要实现WebMvcConfigurer配置接口
public class ConfigurerAdapter implements WebMvcConfigurer {

    private final FileProperties fileProperties;    //文件相关配置

    public ConfigurerAdapter(FileProperties fileProperties) {
        this.fileProperties = fileProperties;
    }

    /**
     * corsFilter用于进行跨域配置。该适配方法用于解决CorsFilter与spring security的冲突，放行所有的跨域请求
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

    /**
     * 添加资源处理器，用于处理一些文件等静态资源的映射
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //获取当前系统对应的文件路径对象用于初始化文件路径对象
        FileProperties.PlPath path = fileProperties.getPath();
        //获取头像图片路径
        String avatarUtl = "file:" + path.getAvatar().replace("\\", "/");
        //获取文件路径
        String fileUtl = "file:" + path.getPath().replace("\\", "/");

        //注冊到资源处理器中
        registry.addResourceHandler("/avatar/**").addResourceLocations(avatarUtl).setCachePeriod(0);
        registry.addResourceHandler("/file/**").addResourceLocations(fileUtl).setCachePeriod(0);
        registry.addResourceHandler("/**").addResourceLocations("classpath:/META-INF/resources/").setCachePeriod(0);
    }
}
