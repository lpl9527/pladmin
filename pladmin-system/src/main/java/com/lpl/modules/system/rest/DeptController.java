package com.lpl.modules.system.rest;

import com.lpl.annotation.Log;
import com.lpl.modules.system.service.DeptService;
import com.lpl.modules.system.service.dto.DeptDto;
import com.lpl.modules.system.service.dto.DeptQueryCriteria;
import com.lpl.utils.PageUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lpl
 * 部门Controller
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：部门管理")
@RequestMapping("/api/dept")
public class DeptController {

    private final DeptService deptService;

    @Log("查询部门")
    @ApiOperation("查询部门")
    @GetMapping
    @PreAuthorize("@pl.check('user:list', 'dept:list')")
    public ResponseEntity<Object> query(DeptQueryCriteria criteria) throws Exception {
        //查询部门列表
        List<DeptDto> deptDtos = deptService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(deptDtos, deptDtos.size()), HttpStatus.OK);
    }
}
