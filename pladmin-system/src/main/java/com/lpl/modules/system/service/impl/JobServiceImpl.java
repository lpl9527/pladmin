package com.lpl.modules.system.service.impl;

import com.lpl.exception.BadRequestException;
import com.lpl.exception.EntityExistException;
import com.lpl.modules.system.domain.Job;
import com.lpl.modules.system.mapstruct.JobMapper;
import com.lpl.modules.system.repository.JobRepository;
import com.lpl.modules.system.repository.UserRepository;
import com.lpl.modules.system.service.JobService;
import com.lpl.modules.system.service.dto.JobDto;
import com.lpl.modules.system.service.dto.JobQueryCriteria;
import com.lpl.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * 岗位业务层接口实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "job")
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final JobMapper jobMapper;
    private final UserRepository userRepository;
    private final RedisUtils redisUtils;

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

    /**
     * 查询全部数据
     * @param criteria
     */
    @Override
    public List<JobDto> queryAll(JobQueryCriteria criteria) {
        List<Job> list = jobRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder));
        return jobMapper.toDto(list);
    }

    /**
     * 创建岗位
     * @param resources
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Job resources) {
        Job job = jobRepository.findByName(resources.getName());
        if(job != null){
            throw new EntityExistException(Job.class,"name",resources.getName());
        }
        //保存用户
        jobRepository.save(resources);
    }

    /**
     * 更新岗位
     * @param job
     */
    @Override
    @CacheEvict(key = "'id:' + #p0.id")
    @Transactional(rollbackFor = Exception.class)
    public void update(Job job) {
       //根据id查询岗位
        Job newJob = jobRepository.findById(job.getId()).orElseGet(Job::new);
        //保证名称不能重复
        Job oldJob = jobRepository.findByName(job.getName());
        if(oldJob != null && !oldJob.getId().equals(job.getId())){
            throw new EntityExistException(Job.class,"name", job.getName());
        }
        ValidationUtil.isNull( job.getId(),"Job","id", job.getId());
        job.setId(newJob.getId());
        jobRepository.save(job);
    }

    /**
     * 批量删除角色
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
       //批量删除
        jobRepository.deleteAllByIdIn(ids);
        //删除缓存
        redisUtils.delByKeys("job::id:", ids);
    }

    /**
     * 导出岗位数据
     * @param jobDtos
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<JobDto> jobDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (JobDto jobDTO : jobDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("岗位名称", jobDTO.getName());
            map.put("岗位状态", jobDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", jobDTO.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 验证是否被用户关联
     * @param ids
     */
    @Override
    public void verification(Set<Long> ids) {
        if(userRepository.countByJobs(ids) > 0){
            throw new BadRequestException("所选的岗位中存在用户关联，请解除关联再试！");
        }
    }
}
