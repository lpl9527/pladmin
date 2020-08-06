package com.lpl.modules.system.rest;

import com.lpl.domain.vo.EmailVo;
import com.lpl.modules.system.service.VerifyService;
import com.lpl.utils.enums.CodeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lpl
 * 系统验证码发送
 */
@Api(tags = "系统：验证码管理")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/code")
public class VerifyController {

    private VerifyService verifyService;

    @ApiOperation("发送邮箱重置验证码")
    @PostMapping(value = "/sendEmailCode")
    public ResponseEntity<Object> sendEmailCode(@RequestParam String email) {
        //发送邮件验证码
        EmailVo emailVo = verifyService.sendEmailCode(email, CodeEnum.EMAIL_RESET_EMAIL_CODE.getKey());
    }
}
