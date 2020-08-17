package com.lpl.modules.system.rest;

import com.lpl.annotation.Log;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.domain.Dict;
import com.lpl.modules.system.service.DictService;
import com.lpl.modules.system.service.dto.DictDto;
import com.lpl.modules.system.service.dto.DictQueryCriteria;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lpl
 * 字典API
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：字典管理")
@RequestMapping("/api/dict")
public class DictController {

    private final DictService dictService;
    private static final String ENTITY_NAME = "dict";

    @Log("查询所有字典")
    @ApiOperation("查询所有字典")
    @GetMapping(value = "/all")
    @PreAuthorize("@pl.check('dict:list')")
    public ResponseEntity<Object> queryAll() {
        //查询全部字典数据
        List<DictDto> dictDtos = dictService.queryAll(new DictQueryCriteria());
        return new ResponseEntity<>(dictDtos, HttpStatus.OK);
    }

    @Log("查询字典")
    @ApiOperation("查询字典")
    @GetMapping
    @PreAuthorize("@pl.check('dict:list')")
    public ResponseEntity<Object> query(DictQueryCriteria criteria, Pageable pageable) {
        Map<String, Object> dicts = dictService.queryAll(criteria, pageable);
        return new ResponseEntity<>(dicts, HttpStatus.OK);
    }

    @Log("新增字典")
    @ApiOperation("新增字典")
    @PostMapping
    @PreAuthorize("@pl.check('dict:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Dict dict) {
        if (dict.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        dictService.create(dict);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改字典")
    @ApiOperation("修改字典")
    @PutMapping
    @PreAuthorize("@pl.check('dict:edit')")
    public ResponseEntity<Object> update(@Validated(Dict.Update.class) @RequestBody Dict dict){
        dictService.update(dict);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除字典")
    @ApiOperation("删除字典")
    @DeleteMapping
    @PreAuthorize("@pl.check('dict:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        dictService.delete(ids);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("导出字典数据")
    @ApiOperation("导出字典数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check('dict:list')")
    public void download(HttpServletResponse response, DictQueryCriteria criteria) throws IOException {
        dictService.download(dictService.queryAll(criteria), response);
    }
}
