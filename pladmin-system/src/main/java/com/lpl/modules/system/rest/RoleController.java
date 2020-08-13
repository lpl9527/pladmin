package com.lpl.modules.system.rest;

import cn.hutool.core.lang.Dict;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.dto.RoleDto;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lpl
 * 角色相关API
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：角色管理")
@RequestMapping("api/roles")
public class RoleController {

    private final RoleService roleService;

    @ApiOperation("获取全部的角色")
    @GetMapping(value = "/all")
    @PreAuthorize("@pl.check('roles:list', 'user:add', 'user:edit')")
    public ResponseEntity<Object> queryAll() {
        List<RoleDto> roleDtos = roleService.queryAll();
        return new ResponseEntity<>(roleDtos, HttpStatus.OK);
    }

    @ApiOperation("获取用户级别")
    @GetMapping(value = "/level")
    public ResponseEntity<Object> getLevel() {
        return new ResponseEntity<>(Dict.create().set("level", getLevels(null)), HttpStatus.OK);
    }

    /**
     * 获取可以操作的最大的角色级别（level值越小，级别越大）
     * @param level 用户想要操作的角色级别
     */
    private int getLevels(Integer level) {
        //获取用户所有的角色级别列表
        List<Integer> levels = roleService.findByUserId(SecurityUtils.getCurrentUserId()).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList());
        //获取最大级别（最小值）
        int min = Collections.min(levels);
        if (level != null) {
            if (level > min) {
                throw  new BadRequestException("权限不足，您的角色级别：" + min + "，低于操作的角色级别：" + level);
            }
        }
        return min;
    }
}
