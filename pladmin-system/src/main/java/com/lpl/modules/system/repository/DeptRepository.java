package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.Dept;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * 部门持久化接口对象
 */
public interface DeptRepository extends JpaRepository<Dept, Long>, JpaSpecificationExecutor {

    /**
     * 根据角色id查询具有操作权限的部门列表
     * @param roleId
     */
    @Query(value = "select d.* from sys_dept d, sys_roles_depts r where d.dept_id = r.dept_id and r.role_id = ?1",
            nativeQuery = true)
    Set<Dept> findByRoleId(Long roleId);

    /**
     * 根据父部门id查询部门列表
     * @param pid
     */
    List<Dept> findByPid(Long pid);

    /**
     * 获取顶级部门
     */
    List<Dept> findByPidIsNull();
}
