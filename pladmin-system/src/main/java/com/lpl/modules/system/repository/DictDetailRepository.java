package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.DictDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author lpl
 * 自定详情持久化接口
 */
public interface DictDetailRepository extends JpaRepository<DictDetail, Long>, JpaSpecificationExecutor<DictDetail> {

}
