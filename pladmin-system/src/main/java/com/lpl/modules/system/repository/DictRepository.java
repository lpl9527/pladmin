package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.Dict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 字典持久化接口
 */
public interface DictRepository extends JpaRepository<Dict, Long>, JpaSpecificationExecutor<Dict> {

    /**
     * 批量删除字典
     * @param ids
     */
    void deleteByIdIn(Set<Long> ids);

    /**
     * 根据id集合查询字典列表
     * @param ids
     */
    List<Dict> findByIdIn(Set<Long> ids);
}
