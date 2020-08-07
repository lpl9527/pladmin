package com.lpl.modules.system.rest;

import com.lpl.domain.vo.EmailVo;
import com.lpl.modules.system.service.VerifyService;
import com.lpl.service.EmailService;
import com.lpl.utils.enums.CodeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.rmi.server.ServerCloneException;

/**
 * @author lpl
 * 系统验证码发送
 */
@Api(tags = "系统：验证码管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/code")
public class VerifyController {

    private final VerifyService verifyService;
    private final EmailService emailService;

    @ApiOperation("发送邮箱重置验证码")
    @PostMapping(value = "/sendEmailCode")
    public ResponseEntity<Object> sendEmailCode(@RequestParam String email) {
        //查询数据库，构建邮件验证码对象
        EmailVo emailVo = verifyService.BuildEmailCodeVO(email, CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey());
        //查询管理员邮箱配置，发送验证码
        emailService.send(emailVo, emailService.find());
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
