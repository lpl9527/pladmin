package com.lpl.repository;

import com.lpl.domain.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author lpl
 * 邮件配置类持久化接口
 */
public interface EmailRepository extends JpaRepository<EmailConfig, Long> {
}
