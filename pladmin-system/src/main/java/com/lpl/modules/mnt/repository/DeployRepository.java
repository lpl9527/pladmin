package com.lpl.modules.mnt.repository;

import com.lpl.modules.mnt.domain.Deploy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

/**
 * @author lpl
 * 部署持久化接口
 */
public interface DeployRepository extends JpaRepository<Deploy, Long>, JpaSpecificationExecutor<Deploy> {

    /**
     * 根据应用id查询部署信息
     * @param appId
     */
    @Query(value = "select * from mnt_deploy where app_id = ?1",
            nativeQuery = true)
    Deploy findDeployByAppId(Long appId);
}
