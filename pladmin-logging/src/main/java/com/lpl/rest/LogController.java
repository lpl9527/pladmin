package com.lpl.rest;

import com.lpl.domain.Log;
import com.lpl.service.LogService;
import com.lpl.service.dto.LogQueryCriteria;
import com.lpl.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lpl
 * 系统日志Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/logs")
@Api(tags = "系统：日志管理")
public class LogController {

    private final LogService logService;

    @GetMapping(value = "/user")
    @ApiOperation("用户日志查询")
    public ResponseEntity<Object> queryUserLog(LogQueryCriteria criteria, Pageable pageable) {
        criteria.setLogType("INFO");
        criteria.setBlurry(SecurityUtils.getCurrentUsername());
        Object logData = logService.queryAllByUser(criteria, pageable);
        return new ResponseEntity<>(logData, HttpStatus.OK);
    }
}
