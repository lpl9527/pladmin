package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.vo.MenuVo;
import com.lpl.modules.system.service.dto.MenuDto;

import java.util.List;

/**
 * @author lpl
 * 菜单Service接口
 */
public interface MenuService {

    /**
     * 获取当前用户的菜单列表
     * @param userId    当前用户id
     */
    List<MenuDto> findByUser(Long userId);

    /**
     * 构建菜单树
     * @param menuDtos  菜单列表
     */
    List<MenuDto> buildTree(List<MenuDto> menuDtos);

    /**
     * 构建菜单树
     * @param menuDtos
     */
    List<MenuVo> buildMenus(List<MenuDto> menuDtos);

}
