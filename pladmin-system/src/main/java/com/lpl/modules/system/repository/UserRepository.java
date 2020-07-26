package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}
