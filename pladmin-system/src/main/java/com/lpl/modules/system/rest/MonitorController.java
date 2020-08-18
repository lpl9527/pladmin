package com.lpl.modules.system.rest;

import com.lpl.modules.system.service.MonitorService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author lpl
 * 系统监控api
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：服务监控")
@RequestMapping("/api/monitor")
public class MonitorController {

    private final MonitorService monitorService;

    @ApiOperation("查询服务监控")
    @GetMapping
    @PreAuthorize("@pl.check('monitor:list')")
    public ResponseEntity<Object> query() {
        //查询服务器信息
        Map<String, Object> serverInfo = monitorService.getServers();
        return new ResponseEntity<>(serverInfo, HttpStatus.OK);
    }
}
