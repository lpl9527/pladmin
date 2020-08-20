package com.lpl.modules.mnt.repository;

import com.lpl.modules.mnt.domain.App;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @author lpl
 * 应用持久化接口
 */
public interface AppRepository extends JpaRepository<App, Long>, JpaSpecificationExecutor<App> {

}
