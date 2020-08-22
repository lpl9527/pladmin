package com.lpl.modules.mnt.repository;

import com.lpl.modules.mnt.domain.Database;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author lpl
 * 数据库持久化接口
 */
public interface DatabaseRepository extends JpaRepository<Database, String>, JpaSpecificationExecutor<Database> {

}
