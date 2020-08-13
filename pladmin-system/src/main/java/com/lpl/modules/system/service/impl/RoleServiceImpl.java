package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.domain.Menu;
import com.lpl.modules.system.domain.Role;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.mapstruct.RoleMapper;
import com.lpl.modules.system.mapstruct.RoleSmallMapper;
import com.lpl.modules.system.repository.RoleRepository;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.dto.RoleDto;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.utils.StringUtils;
import com.lpl.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lpl
 * 角色Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "role")
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;    //角色持久层接口
    private final RoleSmallMapper roleSmallMapper;
    private final RoleMapper roleMapper;

    /**
     * 根据用户id查询用户的角色对象列表
     * @param userId
     */
    @Override
    public List<RoleSmallDto> findByUserId(Long userId) {
        //获取用户的角色集合
        Set<Role> roleSet = roleRepository.findByUserId(userId);
        //将其转为列表
        List<Role> roles = new ArrayList<>(roleSet);
        //最后将其转换为RoleSmallDto列表
        return roleSmallMapper.toDto(roles);
    }

    /**
     * 获取用户已授权的权限信息列表
     * @param userDto
     */
    @Override
    @Cacheable(key = "'auth:' + #p0.id")    //指定缓存key为 auth: + 用户id 的形式
    public List<GrantedAuthority> mapToGrantedAuthorities(UserDto userDto) {

        Set<String> permissions = new HashSet<>();
        //如果是管理员
        if (userDto.getIsAdmin()) {
            permissions.add("admin");
            return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }
        //根据用户id查询角色列表
        Set<Role> roles = roleRepository.findByUserId(userDto.getId());
        permissions = roles.stream().flatMap(role -> role.getMenus().stream()).
                filter(menu -> StringUtils.isNotBlank(menu.getPermission())).
                map(Menu::getPermission).collect(Collectors.toSet());
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    /**
     * 查询全部角色
     */
    @Override
    public List<RoleDto> queryAll() {
        Sort sort = new Sort(Sort.Direction.ASC, "level");
        return roleMapper.toDto(roleRepository.findAll(sort));
    }

    /**
     * 根据id查询角色
     * @param id
     */
    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public RoleDto findById(Long id) {
        Role role = roleRepository.findById(id).orElseGet(Role::new);
        ValidationUtil.isNull(role.getId(), "Role", "id", id);
        return roleMapper.toDto(role);
    }

    /**
     * 根据角色集合查询用户最高角色级别
     * @param roles
     */
    @Override
    public Integer findByRoles(Set<Role> roles) {
        Set<RoleDto> roleDtos = new HashSet<>();
        for (Role role : roles) {
            //根据id查询角色放入集合
            roleDtos.add(findById(role.getId()));
        }
        return Collections.min(roleDtos.stream().map(RoleDto::getLevel).collect(Collectors.toList()));
    }

}
