package com.lpl.modules.system.rest;

import cn.hutool.core.lang.Dict;
import com.lpl.annotation.Log;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.domain.Role;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.dto.RoleDto;
import com.lpl.modules.system.service.dto.RoleQueryCriteria;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.mockito.internal.verification.InOrderWrapper;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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

    private static final String ENTITY_NAME = "role";

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

    @Log("查询角色")
    @ApiOperation("查询角色")
    @GetMapping
    @PreAuthorize("@pl.check('roles:list')")
    public ResponseEntity<Object> query(RoleQueryCriteria criteria, Pageable pageable) {
        Object roles = roleService.queryAll(criteria, pageable);
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @Log("查询单个角色")
    @GetMapping(value = "/{id}")
    @PreAuthorize("@pl.check('roles:list')")
    public ResponseEntity<Object> query(@PathVariable Long id) {
        RoleDto role = roleService.findById(id);
        return new ResponseEntity<>(role, HttpStatus.OK);
    }

    @Log("新增角色")
    @ApiOperation("新增角色")
    @PostMapping
    @PreAuthorize("@pl.check('roles:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Role role) {
        if (null != role.getId()) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        getLevels(role.getLevel());
        //保存
        roleService.create(role);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("编辑角色")
    @ApiOperation("编辑角色")
    @PutMapping
    @PreAuthorize("@pl.check('roles:edit')")
    public ResponseEntity<Object> update(@Validated(Role.Update.class) @RequestBody Role role) {
        //检查是否有操作权限
        getLevels(role.getLevel());
        //更新角色
        roleService.update(role);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除角色")
    @ApiOperation("删除角色")
    @DeleteMapping
    @PreAuthorize("@pl.check('roles:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        //验证所有要删除的角色是否有操作权限
        for (Long id : ids) {
            //根据id查询角色
            RoleDto role = roleService.findById(id);
            getLevels(role.getLevel());
        }
        //验证是否被用户关联，关联过的角色不能删除
        roleService.verification(ids);
        //批量删除角色
        roleService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("导出角色数据")
    @ApiOperation("导出角色数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check('roles:list')")
    public void download(HttpServletResponse response, RoleQueryCriteria criteria) throws IOException {
        roleService.download(roleService.queryAll(criteria), response);
    }

    @Log("修改角色菜单")
    @ApiOperation("修改角色菜单")
    @PutMapping(value = "/menu")
    @PreAuthorize("@pl.check('roles:edit')")
    public ResponseEntity<Object> updateMenu(@RequestBody Role role) {
        //查询角色数据
        RoleDto roleDto = roleService.findById(role.getId());
        //检查权限
        getLevels(roleDto.getLevel());
        //更新
        roleService.updateMenu(role, roleDto);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
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
            if (level < min) {
                throw  new BadRequestException("权限不足，您的角色级别：" + min + "，低于操作的角色级别：" + level);
            }
        }
        return min;
    }
}
