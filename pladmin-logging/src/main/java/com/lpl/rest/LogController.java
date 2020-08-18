package com.lpl.rest;

import com.lpl.annotation.Log;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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
        criteria.setUsername(SecurityUtils.getCurrentUsername());   //设置当前用户名为查询条件
        Object logData = logService.queryAllByUser(criteria, pageable);
        return new ResponseEntity<>(logData, HttpStatus.OK);
    }

    @Log("查询日志")
    @ApiOperation("日志查询")
    @GetMapping
    @PreAuthorize("@pl.check()")
    public ResponseEntity<Object> query(LogQueryCriteria criteria, Pageable pageable) {
        //查询INFO类型的日志
        criteria.setLogType("INFO");
        Object logs = logService.queryAll(criteria, pageable);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @Log("异常日志查询")
    @ApiOperation("异常日志查询")
    @GetMapping(value = "/error")
    @PreAuthorize("@pl.check()")
    public ResponseEntity<Object> queryErrorLog(LogQueryCriteria criteria, Pageable pageable){
        criteria.setLogType("ERROR");
        Object logs = logService.queryAll(criteria, pageable);
        return new ResponseEntity<>(logs, HttpStatus.OK);
    }

    @ApiOperation("日志异常详情查询")
    @GetMapping(value = "/error/{id}")
    @PreAuthorize("@pl.check()")
    public ResponseEntity<Object> queryErrorLogs(@PathVariable Long id){
        return new ResponseEntity<>(logService.findByErrDetail(id), HttpStatus.OK);
    }

    @Log("删除所有INFO日志")
    @ApiOperation("删除所有INFO日志")
    @DeleteMapping(value = "/del/info")
    @PreAuthorize("@pl.check()")
    public ResponseEntity<Object> delAllInfoLog(){
        //删除所有INFO类型日志
        logService.delAllByInfo();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("删除所有ERROR日志")
    @ApiOperation("删除所有ERROR日志")
    @DeleteMapping(value = "/del/error")
    @PreAuthorize("@pl.check()")
    public ResponseEntity<Object> delAllErrorLog(){
        logService.delAllByError();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("导出日志")
    @ApiOperation("导出日志")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check()")
    public void download(HttpServletResponse response, LogQueryCriteria criteria) throws IOException {
        criteria.setLogType("INFO");
        logService.download(logService.queryAll(criteria), response);
    }

    @Log("导出异常日志")
    @ApiOperation("导出异常日志")
    @GetMapping(value = "/error/download")
    @PreAuthorize("@pl.check()")
    public void downloadErrorLog(HttpServletResponse response, LogQueryCriteria criteria) throws IOException {
        criteria.setLogType("ERROR");
        logService.download(logService.queryAll(criteria), response);
    }
}
