package com.lpl.modules.system.service;

import com.lpl.modules.system.service.dto.JobQueryCriteria;
import org.springframework.data.domain.Pageable;

import java.util.Map;

/**
 * @author lpl
 * 岗位Service接口
 */
public interface JobService {

    /**
     * 查询所有岗位数据
     * @param criteria
     * @param pageable
     */
    Map<String, Object> queryAll(JobQueryCriteria criteria, Pageable pageable);
}
