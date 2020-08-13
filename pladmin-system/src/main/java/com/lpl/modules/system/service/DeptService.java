package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.Dept;
import com.lpl.modules.system.service.dto.DeptDto;
import com.lpl.modules.system.service.dto.DeptQueryCriteria;

import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 部门Service接口
 */
public interface DeptService {

    /**
     * 根据角色id查询具有操作权限的部门集合
     * @param roleId
     */
    Set<Dept> findByRoleId(Long roleId);

    /**
     * 根据父部门id查询部门列表
     * @param pid
     */
    List<Dept> findByPid(Long pid);

    /**
     * 根据父部门id和此父部门下的部门列表递归查询所有部门层级下的部门id
     * @param deptPid
     * @param deptList
     */
    List<Long> getDeptChildren(Long deptPid, List<Dept> deptList);

    /**
     * 查询所有部门数据
     * @param criteria
     * @param isQuery
     * @throws Exception
     */
    List<DeptDto> queryAll(DeptQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 根据id查询部门
     * @param id
     */
    DeptDto findById(Long id);

    /**
     * 递归查询部门上级部门列表
     * @param deptDto 本级部门
     * @param depts 上级部门列表
     */
    List<DeptDto> getSuperior(DeptDto deptDto, List<Dept> depts);

    /**
     * 构造树形部门数据
     * @param deptDtos
     */
    Object buildTree(List<DeptDto> deptDtos);
}
