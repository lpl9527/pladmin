package com.lpl.config;

import com.lpl.utils.SecurityUtils;
import org.springframework.data.domain.AuditorAware;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @author lpl
 * 开启Spring Data提供支持审计功能配置：
 *      即由谁在什么时候创建或修改实体。Spring Data提供了在实体类的属性上增加@CreatedBy，@LastModifiedBy，@CreatedDate，@LastModifiedDate注解，
 *      并配置相应的配置项，即可实现审计功能。由系统自动记录createdBy、CreatedDate、lastModifiedBy、lastModifiedDate四个属性的值。
 */
@Component("auditorAware")
public class AuditorConfig implements AuditorAware<String> {

    /**
     * 获取当前操作员标识信息
     */
    @Override
    public Optional<String> getCurrentAuditor() {
        try {
            //以当前用户名作为用户操作信息
            return Optional.of(SecurityUtils.getCurrentUsername());
        }catch (Exception e) {
            //用户定时任务，或者无token调用的情况
            return Optional.of("System");
        }
    }
}
