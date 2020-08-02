package com.lpl.modules.system.rest;

import com.lpl.modules.system.domain.vo.MenuVo;
import com.lpl.modules.system.service.MenuService;
import com.lpl.modules.system.service.dto.MenuDto;
import com.lpl.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 菜单Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    private final MenuService menuService;

    /**
     * 根据当前登录用户获取前端所需菜单
     */
    @RequestMapping("/build")
    public ResponseEntity<Object> buildMenus() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<MenuDto> menuDtoList = menuService.findByUser(SecurityUtils.getCurrentUserId());
        //构建菜单树
        List<MenuDto> menuDtos = menuService.buildTree(menuDtoList);
        List<MenuVo> list = menuService.buildMenus(menuDtos);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }
}
