package com.lpl.modules.system.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 菜单Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/menus")
public class MenuController {

    /**
     * 根据当前登录用户获取前端所需菜单
     */
    @RequestMapping("/build")
    public ResponseEntity<Object> buildMenus() {

    }
}
