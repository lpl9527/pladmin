package com.lpl.modules.system.rest;

import com.lpl.annotation.Log;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Log("查询字典详情")
    @ApiOperation("查询字典详情")
    @GetMapping()
    public ResponseEntity<Object> queryByName(DictDetailQueryCriteria criteria, @PageableDefault(sort = {"dictSort"}, direction = Sort.Direction.ASC)Pageable pageable) {
        Map<String, Object> dictDetails = dictDetailService.queryAll(criteria, pageable);
        return new ResponseEntity<>(dictDetails, HttpStatus.OK);
    }
}
