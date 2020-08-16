package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Set;

/**
 * @author lpl
 * 岗位数据持久化接口
 */
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {

    /**
     * 根据岗位名称查询
     * @param name
     */
    Job findByName(String name);

    /**
     * 根据id集合批量删除岗位
     * @param ids
     */
    void deleteAllByIdIn(Set<Long> ids);
}
