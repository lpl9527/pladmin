package com.lpl.modules.system.rest;

import com.lpl.config.RsaProperties;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.domain.vo.UserPassVo;
import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.utils.RsaUtils;
import com.lpl.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lpl
 * 用户相关API
 */
@Api(tags = "系统：用户管理")
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @ApiOperation("修改密码")
    @PostMapping(value = "/api/users/updatePass")
    public ResponseEntity<Object> updatePass(@RequestBody UserPassVo passVo) throws Exception {
        //Rsa私钥解密密码
        String oldPass = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, passVo.getOldPass());
        String newPass = RsaUtils.decryptByPrivateKey(RsaProperties.privateKey, passVo.getNewPass());

        //根据档期那用户名获取用户信息
        UserDto user = userService.findByName(SecurityUtils.getCurrentUsername());
        if (passwordEncoder.matches(oldPass, user.getPassword())) {
            throw new BadRequestException("修改密码失败，旧密码错误！");
        }
        if (passwordEncoder.matches(newPass, user.getPassword())) {
            throw new BadRequestException("修改密码失败，新密码不能与旧密码相同！");
        }
        //修改密码
        userService.updatePass();
    }
}
