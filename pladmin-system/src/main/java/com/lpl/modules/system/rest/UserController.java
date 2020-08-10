package com.lpl.modules.system.rest;

import com.lpl.annotation.Log;
import com.lpl.config.RsaProperties;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.domain.vo.UserPassVo;
import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.VerifyService;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.utils.RsaUtils;
import com.lpl.utils.SecurityUtils;
import com.lpl.utils.enums.CodeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

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
}
