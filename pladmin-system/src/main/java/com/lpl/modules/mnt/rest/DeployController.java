package com.lpl.modules.mnt.rest;

import com.lpl.annotation.Log;
import com.lpl.modules.mnt.domain.Deploy;
import com.lpl.modules.mnt.service.DeployService;
import com.lpl.modules.mnt.service.dto.DeployQueryCriteria;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * @author lpl
 * 部署api
 */
@RestController
@Api(tags = "运维：部署管理")
@RequiredArgsConstructor
@RequestMapping("/api/deploy")
public class DeployController {

    private final String fileSavePath = FileUtils.getTmpDirPath() + "/";   //文件临时目录
    private final DeployService deployService;

    @Log("查询部署")
    @ApiOperation(value = "查询部署")
    @GetMapping
    @PreAuthorize("@pl.check('deploy:list')")
    public ResponseEntity<Object> query(DeployQueryCriteria criteria, Pageable pageable){
        return new ResponseEntity<>(deployService.queryAll(criteria,pageable), HttpStatus.OK);
    }

    @Log("新增部署")
    @ApiOperation(value = "新增部署")
    @PostMapping
    @PreAuthorize("@pl.check('deploy:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Deploy deploy){
        deployService.create(deploy);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改部署")
    @ApiOperation(value = "修改部署")
    @PutMapping
    @PreAuthorize("@pl.check('deploy:edit')")
    public ResponseEntity<Object> update(@Validated @RequestBody Deploy deploy){
        deployService.update(deploy);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除部署")
    @ApiOperation(value = "删除部署")
    @DeleteMapping
    @PreAuthorize("@pl.check('deploy:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        deployService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("导出部署数据")
    @ApiOperation("导出部署数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check('deploy:list')")
    public void download(HttpServletResponse response, DeployQueryCriteria criteria) throws IOException {
        deployService.download(deployService.queryAll(criteria), response);
    }

    //----------------------------------------------------------------------------------------------------

    @Log("上传文件部署")
    @ApiOperation("上传文件部署")
    @PostMapping(value = "/upload")
    @PreAuthorize("@pl.check('deploy:edit')")
    public ResponseEntity<Object> upload(@RequestBody MultipartFile file, HttpServletRequest request) throws Exception {
        //获取部署的应用id
        Long appId = Long.valueOf(request.getParameter("id"));
        String fileName = "";
        if (file != null) {
            fileName = file.getOriginalFilename();
            //部署的文件对象
            File deployFile = new File(fileSavePath + fileName);
            FileUtils.del(deployFile);
            file.transferTo(deployFile);
            //上传部署应用
            deployService.deploy(fileSavePath+fileName , appId);
        }else {
            System.out.println("没有找到相对应的文件");
        }
        System.out.println("文件上传的原名称为:"+ Objects.requireNonNull(file).getOriginalFilename());
        Map<String,Object> map = new HashMap<>(2);
        map.put("errno", 0);
        map.put("id", fileName);
        return new ResponseEntity<>(map,HttpStatus.OK);
    }
}
