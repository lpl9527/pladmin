package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Set;

/**
 * @author lpl
 * 角色持久化接口
 */
public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    /**
     * 根据用户id查询角色列表
     * @param userId
     */
    @Query(value = "select r.* from sys_role r, sys_users_roles u where r.role_id = u.role_id and u.user_id = ?1",
            nativeQuery = true)
    Set<Role> findByUserId(Long userId);

}
