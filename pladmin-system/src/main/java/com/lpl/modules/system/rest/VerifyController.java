package com.lpl.modules.system.rest;

import com.lpl.domain.vo.EmailVo;
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

    @ApiOperation("发送邮箱重置验证码")
    @PostMapping(value = "/sendEmailCode")
    public ResponseEntity<Object> sendEmailCode(@RequestParam String email) {
        EmailVo emailVo =
    }
}
