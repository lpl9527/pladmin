package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;
import java.util.List;
import java.util.Set;

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
     * 根据邮箱查询用户
     * @param email
     */
    User findByEmail(String email);

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

    /**
     * 根据id列表批量删除
     * @param ids
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据角色集合查询用户数量
     * @param ids
     */
    @Query(value = "select count(1) from sys_user u, sys_users_roles r where u.user_id = r.user_id and r.role_id in ?1 ",
            nativeQuery = true)
    int countByRoles(Set<Long> ids);

    /**
     * 根据角色id查询关联的用户集合
     * @param roleId
     */
    @Query(value = "select u.* from sys_user u, sys_users_roles r where u.user_id = r.user_id and r.role_id = ?1",
            nativeQuery = true)
    List<User> findByRoleId(Long roleId);

    /**
     * 根据菜单id查询用户
     * @param id
     */
    @Query(value = "select u.* from sys_user u, sys_users_roles ur, sys_roles_menus rm where u.user_id = ur.user_id and ur.role_id = rm.role_id and rm.menu_id = ?1 group by u.user_id",
            nativeQuery = true)
    List<User> findByMenuId(Long id);

    /**
     * 根据部门id查询用户列表
     * @param id
     */
    @Query(value = "select u.* from sys_user u, sys_users_roles r, sys_roles_depts d where u.user_id = r.user_id and r.role_id = d.role_id and r.role_id = ?1 group by u.user_id",
            nativeQuery = true)
    List<User> findByDeptId(Long id);

    /**
     * 根据部门id集合查询关联用户数量
     * @param deptIds
     */
    @Query(value = "select count(1) from sys_user u where u.dept_id in ?1",
            nativeQuery = true)
    int countByDepts(Set<Long> deptIds);

    /**
     * 根据岗位id集合查询关联用户数量
     * @param ids
     * @return
     */
    @Query(value = "select count(1) from sys_user u, sys_users_jobs j where u.user_id = j.user_id and j.job_id in ?1",
            nativeQuery = true)
    int countByJobs(Set<Long> ids);

}
