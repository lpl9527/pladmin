package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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

    /**
     * 根据名称查询
     * @param name
     */
    Role findByName(String name);

    /**
     * 批量删除用户
     * @param ids
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据菜单id查询角色列表
     * @param menuIds
     */
    @Query(value = "select r.* from sys_role r, sys_roles_menus m where r.role_id = m.role_id and m.menu_id in ?1",
            nativeQuery = true)
    List<Role> findInMenuId(List<Long> menuIds);

    /**
     * 解绑角色菜单，删除菜单角色关系
     * @param menuId
     */
    @Modifying
    @Query(value = "delete from sys_roles_menus where menu_id = ?1",
            nativeQuery = true)
    void untiedMenu(Long menuId);

    /**
     * 根据部门id结合查询关联角色对象
     * @param deptIds
     */
    @Query(value = "select count(1) from sys_role r, sys_roles_depts d where r.role_id = d.role_id and d.dept_id in ?1",
            nativeQuery = true)
    int countByDepts(Set<Long> deptIds);
}
