package com.lpl.modules.security.rest;

import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *  @author lpl
 *  提供授权，根据token获取用户详细信息
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：系统授权接口")
public class AuthorizationController {


}
