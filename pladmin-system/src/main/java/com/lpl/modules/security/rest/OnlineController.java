package com.lpl.modules.security.rest;

import com.lpl.annotation.Log;
import com.lpl.modules.security.service.OnlineUserService;
import com.lpl.utils.EncryptUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * @author lpl
 * 在线用户API
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/online")
@Api(tags = "系统：在线用户管理")
public class OnlineController {

    private final OnlineUserService onlineUserService;

    @Log("查询在线用户")
    @ApiOperation("查询在线用户")
    @GetMapping
    @PreAuthorize("@pl.check()")
    public ResponseEntity<Object> query(String filter, Pageable pageable) {
        //分页查询在线用户
        Map<String, Object> onlineUsers = onlineUserService.getAll(filter, pageable);
        return new ResponseEntity<>(onlineUsers, HttpStatus.OK);
    }

    @Log("导出在线用户")
    @ApiOperation("导出在线用户")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check()")
    public void download(HttpServletResponse response, String filter) throws IOException {
        onlineUserService.download(onlineUserService.getAll(filter), response);
    }
    @Log("踢出在线用户")
    @ApiOperation("踢出在线用户")
    @DeleteMapping
    @PreAuthorize("@pl.check()")
    public ResponseEntity<Object> delete(@RequestBody Set<String> keys) throws Exception {
        for (String key : keys) {
            // 解密Key
            key = EncryptUtils.desDecrypt(key);
            onlineUserService.kickOut(key);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
