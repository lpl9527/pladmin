package com.lpl;

import com.lpl.annotation.AnonymousAccess;
import com.lpl.utils.SpringContextHolder;
import io.swagger.annotations.Api;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableAsync    //开启异步
@RestController
@Api(hidden = true)
@SpringBootApplication
@EnableTransactionManagement    //开启事务
@EnableJpaAuditing(auditorAwareRef = "auditorAware")    //开启支持Jpa监控属性的变化，指定配置为注入的auditorAware组件
public class PlAdminApp {

    public static void main(String[] args) {
        SpringApplication.run(PlAdminApp.class, args);
    }

    @Bean
    public SpringContextHolder springContextHolder() {
        return new SpringContextHolder();
    }

    /**
     * 访问首页
     */
    @GetMapping("/")
    @AnonymousAccess
    public String index() {
        return "service start success!";
    }
}
