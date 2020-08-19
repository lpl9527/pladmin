package com.lpl.modules.mnt.rest;

import com.lpl.annotation.Log;
import com.lpl.modules.mnt.domain.ServerDeploy;
import com.lpl.modules.mnt.service.ServerDeployService;
import com.lpl.modules.mnt.service.dto.ServerDeployQueryCriteria;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author lpl
 * 服务器部署api
 */
@RestController
@Api(tags = "运维：服务器管理")
@RequiredArgsConstructor
@RequestMapping("/api/serverDeploy")
public class ServerDeployController {

    private final ServerDeployService serverDeployService;

    @Log("查询服务器")
    @ApiOperation(value = "查询服务器")
    @GetMapping
    @PreAuthorize("@pl.check('serverDeploy:list')")
    public ResponseEntity<Object> query(ServerDeployQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(serverDeployService.queryAll(criteria, pageable), HttpStatus.OK);
    }

    @Log("新增服务器")
    @ApiOperation(value = "新增服务器")
    @PostMapping
    @PreAuthorize("@pl.check('serverDeploy:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody ServerDeploy serverDeploy){
        serverDeployService.create(serverDeploy);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改服务器")
    @ApiOperation(value = "修改服务器")
    @PutMapping
    @PreAuthorize("@pl.check('serverDeploy:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody ServerDeploy serverDeploy){
        serverDeployService.update(serverDeploy);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除服务器")
    @ApiOperation(value = "删除服务器")
    @DeleteMapping
    @PreAuthorize("@pl.check('serverDeploy:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        serverDeployService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("导出服务器")
    @ApiOperation("导出服务器")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check('serverDeploy:list')")
    public void download(HttpServletResponse response, ServerDeployQueryCriteria criteria) throws IOException {
        serverDeployService.download(serverDeployService.queryAll(criteria), response);
    }

    @Log("测试连接服务器")
    @ApiOperation(value = "测试连接服务器")
    @PostMapping("/testConnect")
    @PreAuthorize("@pl.check('serverDeploy:add')")
    public ResponseEntity<Object> testConnect(@Validated @RequestBody ServerDeploy serverDeploy){
        return new ResponseEntity<>(serverDeployService.testConnect(serverDeploy),HttpStatus.CREATED);
    }
}
