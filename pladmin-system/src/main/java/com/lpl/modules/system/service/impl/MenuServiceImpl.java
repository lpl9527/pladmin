package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.service.MenuService;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.dto.MenuDto;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lpl
 * 菜单Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "menu")
public class MenuServiceImpl implements MenuService {

    private final RoleService roleService;

    /**
     * 获取当前用户的菜单列表
     * @param userId
     */
    @Override
    @Cacheable(key = "'user:' + #p0")   //指定属于user的缓存
    public List<MenuDto> findByUser(Long userId) {

        //根据当前用户id获取用户的角色列表
        List<RoleSmallDto> roles = roleService.findByUserId(userId);
        //将角色列表提取为角色id集合
        Set<Long> roleIds = roles.stream().map(RoleSmallDto::getId).collect(Collectors.toSet());
        return null;
    }
}
