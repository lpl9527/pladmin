package com.lpl.modules.mnt.repository;

import com.lpl.modules.mnt.domain.DeployHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface DeployHistoryRepository extends JpaRepository<DeployHistory, String>, JpaSpecificationExecutor<DeployHistory> {

    /**
     * 根据部署id删除部署历史
     * @param deployId
     */
    @Modifying
    @Query(value = "delete from mnt_deploy_history where deploy_id = ?1",
            nativeQuery = true)
    void deleteByDeployId(Long deployId);
}
