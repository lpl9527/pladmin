package com.lpl.modules.system.rest;

import com.lpl.annotation.Log;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.domain.DictDetail;
import com.lpl.modules.system.service.DictDetailService;
import com.lpl.modules.system.service.dto.DictDetailQueryCriteria;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author lpl
 * 数据字典Controller
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：字典详情管理")
@RequestMapping("/api/dictDetail")
public class DictDetailController {

    private final DictDetailService dictDetailService;
    private static final String ENTITY_NAME = "dictDetail";

    @Log("查询字典详情")
    @ApiOperation("查询字典详情")
    @GetMapping()
    public ResponseEntity<Object> queryByName(DictDetailQueryCriteria criteria, @PageableDefault(sort = {"dictSort"}, direction = Sort.Direction.ASC)Pageable pageable) {
        Map<String, Object> dictDetails = dictDetailService.queryAll(criteria, pageable);
        return new ResponseEntity<>(dictDetails, HttpStatus.OK);
    }

    @Log("新增字典详情")
    @ApiOperation("新增字典详情")
    @PostMapping
    @PreAuthorize("@pl.check('dict:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody DictDetail dictDetail){
        if (dictDetail.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        dictDetailService.create(dictDetail);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("修改字典详情")
    @ApiOperation("修改字典详情")
    @PutMapping
    @PreAuthorize("@pl.check('dict:edit')")
    public ResponseEntity<Object> update(@Validated(DictDetail.Update.class) @RequestBody DictDetail dictDetail){
        dictDetailService.update(dictDetail);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("删除字典详情")
    @ApiOperation("删除字典详情")
    @DeleteMapping(value = "/{id}")
    @PreAuthorize("@pl.check('dict:del')")
    public ResponseEntity<Object> delete(@PathVariable Long id){
        dictDetailService.delete(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
