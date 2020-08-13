package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.domain.Job;
import com.lpl.modules.system.mapstruct.JobMapper;
import com.lpl.modules.system.repository.JobRepository;
import com.lpl.modules.system.service.JobService;
import com.lpl.modules.system.service.dto.JobQueryCriteria;
import com.lpl.utils.PageUtil;
import com.lpl.utils.QueryHelp;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 岗位业务层接口实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "job")
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;

    /**
     * 查询所有岗位数据
     * @param criteria
     * @param pageable
     */
    @Override
    public Map<String, Object> queryAll(JobQueryCriteria criteria, Pageable pageable) {
        Page<Job> page = jobRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(jobMapper::toDto).getContent(), page.getTotalElements());
    }
}
