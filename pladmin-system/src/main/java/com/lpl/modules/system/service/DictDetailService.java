package com.lpl.modules.system.service;

import com.lpl.modules.system.service.dto.DictDetailQueryCriteria;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @author lpl
 * 字典详情业务层接口
 */
public interface DictDetailService {

    /**
     * 分页查询数据字典详情
     * @param criteria 查询条件
     * @param pageable 分页参数
     */
    Map<String, Object> queryAll(DictDetailQueryCriteria criteria, Pageable pageable);
}
