package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.Job;
import com.lpl.modules.system.service.dto.JobDto;
import com.lpl.modules.system.service.dto.JobQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * 查询全部数据
     * @param criteria
     */
    List<JobDto> queryAll(JobQueryCriteria criteria);

    /**
     * 创建岗位
     * @param job
     */
    void create(Job job);

    /**
     * 更新岗位
     * @param job
     */
    void update(Job job);

    /**
     * 批量删除角色
     * @param ids
     */
    void delete(Set<Long> ids);

    /**
     * 导出岗位数据
     * @param jobDtos
     * @param response
     * @throws IOException
     */
    void download(List<JobDto> jobDtos, HttpServletResponse response) throws IOException;

    /**
     * 验证是否被用户关联
     * @param ids
     */
    void verification(Set<Long> ids);
}
