package com.lpl.repository;

import com.lpl.domain.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * @author lpl
 * 日志持久化接口
 */
@Repository
public interface LogRepository extends JpaRepository<Log, Long>, JpaSpecificationExecutor<Log> {
}
