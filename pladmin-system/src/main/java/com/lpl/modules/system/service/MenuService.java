package com.lpl.modules.system.service;

import com.lpl.modules.system.service.dto.MenuDto;

import java.util.List;

/**
 * @author lpl
 * 菜单Service接口
 */
public interface MenuService {

    /**
     * 获取当前用户的菜单列表
     * @param userId
     */
    List<MenuDto> findByUser(Long userId);
}
