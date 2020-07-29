package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.domain.Menu;
import com.lpl.modules.system.mapstruct.MenuMapper;
import com.lpl.modules.system.repository.MenuRepository;
import com.lpl.modules.system.service.MenuService;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.dto.MenuDto;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
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
    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    /**
     * 获取当前用户的菜单列表
     * @param userId 当前用户id
     */
    @Override
    @Cacheable(key = "'user:' + #p0")   //指定属于user的缓存
    public List<MenuDto> findByUser(Long userId) {

        //根据当前用户id获取用户的角色列表
        List<RoleSmallDto> roles = roleService.findByUserId(userId);
        //将角色列表提取为角色id集合
        Set<Long> roleIds = roles.stream().map(RoleSmallDto::getId).collect(Collectors.toSet());
        //根据角色id结合查询非按钮类型菜单集合
        LinkedHashSet<Menu> menus = menuRepository.findByRoleIdsAndTypeNot(roleIds, 2);     //类型2表示按钮类型菜单
        //将菜单集合转化为菜单Dto列表
        List<MenuDto> menuDtoList = menus.stream().map(menuMapper::toDto).collect(Collectors.toList());

        return menuDtoList;
    }

    /**
     * 构建菜单树
     * @param menuDtos  菜单列表
     */
    @Override
    public List<MenuDto> buildTree(List<MenuDto> menuDtos) {
        List<MenuDto> tree = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (MenuDto menuDto : menuDtos) {
            //如果是根目录
            if (menuDto.getPid() == null) {
                tree.add(menuDto);
            }
            //不是根目录的收集将每个菜单的子菜单汇总，并收集菜单id集合
            for (MenuDto it : menuDtos) {
                //如果当前菜单下有子菜单
                if (menuDto.getId().equals(it.getPid())) {
                    //没有子菜单时
                    if (menuDto.getChildren() == null) {
                        menuDto.setChildren(new ArrayList<>());
                    }
                    //将此子菜单添加到当前菜单中
                    menuDto.getChildren().add(it);
                    //id放到菜单列表中
                    ids.add(it.getId());
                }
            }
        }
        //如果都没有根目录（都是一级菜单）
        if (tree.size() == 0) {
            tree = menuDtos.stream().filter(s -> !)
        }
        return null;
    }
}
