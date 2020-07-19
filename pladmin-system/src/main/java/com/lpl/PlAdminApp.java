package com.lpl;

import com.lpl.annotation.AnonymousAccess;
import com.lpl.utils.SpringContextHolder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
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
