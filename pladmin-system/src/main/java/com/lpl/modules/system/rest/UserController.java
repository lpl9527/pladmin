package com.lpl.modules.system.rest;

import cn.hutool.core.collection.CollectionUtil;
import com.lpl.annotation.Log;
import com.lpl.config.RsaProperties;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.domain.vo.UserPassVo;
import com.lpl.modules.system.service.*;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.modules.system.service.dto.UserQueryCriteria;
import com.lpl.utils.PageUtil;
import com.lpl.utils.RsaUtils;
import com.lpl.utils.SecurityUtils;
import com.lpl.utils.enums.CodeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lpl
 * 用户相关API
 */
@Api(tags = "系统：用户管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final VerifyService verifyService;
    private final DeptService deptService;
    private final DataService dataService;
    private final RoleService roleService;

    @Log("查询用户")
    @ApiOperation("查询用户")
    @GetMapping
    @PreAuthorize("@pl.check('user:list')")
    public ResponseEntity<Object> query(UserQueryCriteria criteria, Pageable pageable) {
        //如果有部门查询条件，则查询出该部门下的所有子部门放入部门集合列表条件中
        if (!ObjectUtils.isEmpty(criteria.getDeptId())) {
            //现将本部门加入
            criteria.getDeptIds().add(criteria.getDeptId());
            //查询子部门下的部门id列表加入
            criteria.getDeptIds().addAll(deptService.getDeptChildren(criteria.getDeptId(), deptService.findByPid(criteria.getDeptId())));
        }
        //获取当前用户具有的数据权限部门id列表
        List<Long> dataScopesDeptIds = dataService.getDeptIds(userService.findByName(SecurityUtils.getCurrentUsername()));
        //查询条件不为空并且具有数据权限部门id列表不为空则条件取交集，否则取并集
        if (!CollectionUtils.isEmpty(criteria.getDeptIds()) && !CollectionUtils.isEmpty(dataScopesDeptIds)) {
            //取交集
            criteria.getDeptIds().retainAll(dataScopesDeptIds);
            //查询用户
            if(!CollectionUtil.isEmpty(criteria.getDeptIds())) {
                Object users = userService.queryAll(criteria, pageable);
                return new ResponseEntity<>(users, HttpStatus.OK);
            }
        }else {
            //取并集
            criteria.getDeptIds().addAll(dataScopesDeptIds);
            //查询用户
            Object users = userService.queryAll(criteria, pageable);
            return new ResponseEntity<>(users, HttpStatus.OK);
        }
        return new ResponseEntity<>(PageUtil.toPage(null,0),HttpStatus.OK);
    }

    @ApiOperation("修改密码")
    @PostMapping(value = "/updatePass")
    public ResponseEntity<Object> updatePass(@RequestBody UserPassVo passVo) throws Exception {
        //Rsa私钥解密密码
        String oldPass = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, passVo.getOldPass());
        String newPass = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, passVo.getNewPass());

        //根据档期那用户名获取用户信息
        UserDto user = userService.findByName(SecurityUtils.getCurrentUsername());
        if (!passwordEncoder.matches(oldPass, user.getPassword())) {
            throw new BadRequestException("修改密码失败，旧密码错误！");
        }
        if (passwordEncoder.matches(newPass, user.getPassword())) {
            throw new BadRequestException("修改密码失败，新密码不能与旧密码相同！");
        }
        //修改密码
        userService.updatePass(user.getUsername(), passwordEncoder.encode(newPass));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("修改邮箱")
    @ApiOperation("修改邮箱")
    @PostMapping(value = "/updateEmail/{code}")
    public ResponseEntity<Object> updateEmail(@PathVariable String code, @RequestBody User user) throws Exception {
        //RSA私钥解密密码
        String password = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, user.getPassword());
        //根据用户名查询用户信息
        UserDto userDto = userService.findByName(SecurityUtils.getCurrentUsername());
        if (!passwordEncoder.matches(password, userDto.getPassword())) {
            throw new BadRequestException("密码错误！");
        }
        //验证邮箱验证码
        verifyService.validatedCode(CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey() + user.getEmail(), code);
        //更新邮箱
        userService.updateEmail(userDto.getUsername(), user.getEmail());

        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("修改头像")
    @ApiOperation("修改头像")
    @PostMapping(value = "/updateAvatar")
    public ResponseEntity<Object> updateAvatar(@RequestParam MultipartFile avatar) {
        return new ResponseEntity<>(userService.updateAvatar(avatar), HttpStatus.OK);
    }

    @Log("修改用户：个人中心")
    @ApiOperation("修改用户：个人中心")
    @PutMapping(value = "/center")
    public ResponseEntity<Object> editUser(@Validated(User.Update.class) @RequestBody User  user) {
        if (!user.getId().equals(SecurityUtils.getCurrentUserId())) {
            throw new BadRequestException("不能修改他人资料");
        }
        //更新用户
        userService.updateCenter(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("新增用户")
    @ApiOperation("新增用户")
    @PostMapping
    public ResponseEntity<Object> create(@Validated @RequestBody User user) {
        checkLevel(user);
        //设置默认密码为："123456"
        user.setPassword(passwordEncoder.encode("123456"));
        userService.create(user);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除用户")
    @ApiOperation("删除用户")
    @DeleteMapping
    @PreAuthorize("@pl.check('user:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids) {
        for (Long id : ids) {
            //根据当前用户id获取用户最高角色权限
            Integer currentLevel = Collections.min(roleService.findByUserId(SecurityUtils.getCurrentUserId()).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
            //获取要删除用户的最高权限
            Integer optLevel = Collections.min(roleService.findByUserId(id).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
            if (currentLevel > optLevel) {
                throw new BadRequestException("角色权限不足，不能删除：" + userService.findById(id).getUsername());
            }
        }
        //批量删除用户
        userService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    /**
     * 权限检查，如果当前用户的角色级别低于创建用户的角色级别，则抛出权限不足
     * @param user
     */
    private void checkLevel(User user) {
        //获取当前用户的最高权限级别
        Integer currentLevel = Collections.min(roleService.findByUserId(SecurityUtils.getCurrentUserId()).stream().map(RoleSmallDto::getLevel).collect(Collectors.toList()));
        //获取要创建用户的最高角色级别
        Integer optLevel = roleService.findByRoles(user.getRoles());
        if (currentLevel > optLevel) {
            throw new BadRequestException("角色权限不足！");
        }
    }

    @Log("导出用户数据")
    @ApiOperation("导出用户数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check('user:list')")
    public void download(HttpServletResponse response, UserQueryCriteria criteria) throws IOException {
        userService.download(userService.queryAll(criteria), response);
    }

    @Log("编辑用户")
    @ApiOperation("编辑用户")
    @PutMapping
    @PreAuthorize("@pl.check('user:edit')")
    public ResponseEntity<Object> update(@Validated(User.Update.class) @RequestBody User user) {
        //检查用户权限
        checkLevel(user);
        //更新
        userService.update(user);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
