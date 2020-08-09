package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * @author lpl
 * 用户持久化接口
 */
//Spring Data Jpa同样提供了类似Hibernated 的Criteria的查询方式，要使用这种方式只要继承JpaSpecificationExecutor
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查询用户
     * @param username
     */
    User findByUsername(String username);

    /**
     * 修改密码
     * @param username
     * @param encryptPass
     * @param lastPasswordResetTime 密码最后修改时间
     */
    @Modifying
    @Query(value = "update sys_user set password = ?2, pwd_reset_time = ?3 where username = ?1",
            nativeQuery = true)
    void updatePass(String username, String encryptPass, Date lastPasswordResetTime);

    /**
     * 根据用户名更新用户邮箱
     * @param username  用户名
     * @param email 新邮箱
     */
    @Modifying
    @Query(value = "update sys_user set email = ?2 where username = ?1",
            nativeQuery = true)
    void updateEmail(String username, String email);
}
