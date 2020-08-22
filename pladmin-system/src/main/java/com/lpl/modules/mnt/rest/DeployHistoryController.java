package com.lpl.modules.mnt.rest;

import com.lpl.annotation.Log;
import com.lpl.modules.mnt.service.DeployHistoryService;
import com.lpl.modules.mnt.service.dto.DeployHistoryQueryCriteria;
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
import java.util.Set;

/**
 * @author lpl
 * 部署历史api
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "运维：部署历史管理")
@RequestMapping("/api/deployHistory")
public class DeployHistoryController {

    private final DeployHistoryService deployhistoryService;

    @Log("查询部署历史")
    @ApiOperation(value = "查询部署历史")
    @GetMapping
    @PreAuthorize("@pl.check('deployHistory:list')")
    public ResponseEntity<Object> query(DeployHistoryQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(deployhistoryService.queryAll(criteria,pageable), HttpStatus.OK);
    }

    @Log("删除部署历史")
    @ApiOperation(value = "删除部署历史")
    @DeleteMapping
    @PreAuthorize("@pl.check('deployHistory:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<String> ids){
        deployhistoryService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("导出部署历史数据")
    @ApiOperation("导出部署历史数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check('deployHistory:list')")
    public void download(HttpServletResponse response, DeployHistoryQueryCriteria criteria) throws IOException {
        deployhistoryService.download(deployhistoryService.queryAll(criteria), response);
    }
}
