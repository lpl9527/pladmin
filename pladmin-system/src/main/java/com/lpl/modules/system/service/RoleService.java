package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.Role;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.service.dto.RoleDto;
import com.lpl.modules.system.service.dto.RoleQueryCriteria;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.modules.system.service.dto.UserDto;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 角色Service接口
 */
public interface RoleService {

    /**
     * 根据用户id查询用户的角色对象列表
     * @param userId
     */
    List<RoleSmallDto> findByUserId(Long userId);

    /**
     * 获取用户已授权的权限信息列表
     * @param userDto
     */
    List<GrantedAuthority> mapToGrantedAuthorities(UserDto userDto);

    /**
     * 查询全部数据
     */
    List<RoleDto> queryAll();

    /**
     * 带条件分页查询角色
     * @param criteria
     * @param pageable
     */
    Object queryAll(RoleQueryCriteria criteria, Pageable pageable);

    /**
     * 带条件，不分页查询全部角色数据
     * @param criteria
     */
    List<RoleDto> queryAll(RoleQueryCriteria criteria);

    /**
     * 根据id查询角色
     * @param id
     */
    RoleDto findById(Long id);

    /**
     * 根据角色集合查询用户最高角色级别
     * @param roles
     */
    Integer findByRoles(Set<Role> roles);

    /**
     * 创建角色
     * @param role
     */
    void create(Role role);

    /**
     * 更新角色
     * @param role
     */
    void update(Role role);

    /**
     * 批量删除角色
     * @param ids
     */
    void delete(Set<Long> ids);

    /**
     * 验证角色集合是否有被用户关联的角色
     * @param ids
     */
    void verification(Set<Long> ids);

    /**
     * 导出角色数据
     * @param roles 待导出的角色数据
     * @param response
     * @throws IOException
     */
    void download(List<RoleDto> roles, HttpServletResponse response) throws IOException;

    /**
     * 更新角色菜单
     * @param role
     * @param roleDto
     */
    void updateMenu(Role role, RoleDto roleDto);

    /**
     * 根据菜单id查询角色列表
     * @param menuIds
     */
    List<Role> findInMenuId(List<Long> menuIds);

    /**
     * 根据菜单id解绑角色
     * @param id
     */
    void untiedMenu(Long id);

}
