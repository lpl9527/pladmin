package com.lpl.modules.mnt.rest;

import com.lpl.annotation.Log;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.mnt.domain.Database;
import com.lpl.modules.mnt.service.DatabaseService;
import com.lpl.modules.mnt.service.dto.DatabaseDto;
import com.lpl.modules.mnt.service.dto.DatabaseQueryCriteria;
import com.lpl.modules.mnt.uitl.SqlUtils;
import com.lpl.utils.FileUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * @author lpl
 * 数据库api
 */
@RestController
@Api(tags = "运维：数据库管理")
@RequiredArgsConstructor
@RequestMapping("/api/database")
public class DatabaseController {

    private final String fileSavePath = FileUtils.getTmpDirPath() + "/";
    private final DatabaseService databaseService;


    @Log("查询数据库")
    @ApiOperation(value = "查询数据库")
    @GetMapping
    @PreAuthorize("@pl.check('database:list')")
    public ResponseEntity<Object> query(DatabaseQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(databaseService.queryAll(criteria,pageable), HttpStatus.OK);
    }

    @Log("新增数据库")
    @ApiOperation(value = "新增数据库")
    @PostMapping
    @PreAuthorize("@pl.check('database:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Database resources){
        databaseService.create(resources);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改数据库")
    @ApiOperation(value = "修改数据库")
    @PutMapping
    @PreAuthorize("@pl.check('database:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody Database resources){
        databaseService.update(resources);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除数据库")
    @ApiOperation(value = "删除数据库")
    @DeleteMapping
    @PreAuthorize("@pl.check('database:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<String> ids){
        databaseService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("导出数据库数据")
    @ApiOperation("导出数据库数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check('database:list')")
    public void download(HttpServletResponse response, DatabaseQueryCriteria criteria) throws IOException {
        databaseService.download(databaseService.queryAll(criteria), response);
    }

    @Log("测试数据库连接")
    @ApiOperation(value = "测试数据库连接")
    @PostMapping("/testConnect")
    @PreAuthorize("@pl.check('database:testConnect')")
    public ResponseEntity<Object> testConnect(@Validated @RequestBody Database resources){
        return new ResponseEntity<>(databaseService.testConnection(resources),HttpStatus.CREATED);
    }

    @Log("执行SQL脚本")
    @ApiOperation(value = "执行SQL脚本")
    @PostMapping(value = "/upload")
    @PreAuthorize("@pl.check('database:add')")
    public ResponseEntity<Object> upload(@RequestBody MultipartFile file, HttpServletRequest request) throws Exception {
        String databaseId = request.getParameter("id");
        //获取数据库信息
        DatabaseDto database = databaseService.findById(databaseId);

        String fileName;
        if (null != database) {
            fileName = file.getOriginalFilename();
            File executeFile = new File(fileSavePath + fileName);
            FileUtils.del(executeFile);
            file.transferTo(executeFile);
            //执行数据库脚本
            String result = SqlUtils.executeFile(database.getJdbcUrl(), database.getUserName(), database.getPwd(), executeFile);
            return new ResponseEntity<>(result, HttpStatus.OK);
        }else {
            throw new BadRequestException("Database not exist");
        }
    }
}
