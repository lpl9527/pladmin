package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.Role;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.service.dto.RoleDto;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.modules.system.service.dto.UserDto;
import org.springframework.security.core.GrantedAuthority;

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
     * 根据id查询角色
     * @param id
     */
    RoleDto findById(Long id);

    /**
     * 根据角色集合查询用户最高角色级别
     * @param roles
     */
    Integer findByRoles(Set<Role> roles);

}
