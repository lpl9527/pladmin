package com.lpl.modules.mnt.repository;

import com.lpl.modules.mnt.domain.ServerDeploy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * @author lpl
 * 服务器部署持久化接口
 */
public interface ServerDeployRepository extends JpaRepository<ServerDeploy, Long>, JpaSpecificationExecutor<ServerDeploy> {

    /**
     * 根据ip查询服务器信息
     * @param ip
     */
    ServerDeploy findByIp(String ip);

    /**
     * 根据部署id删除关联的服务器信息
     * @param deployId
     */
    @Modifying
    @Query(value = "delete from mnt_deploy_server where deploy_id = ?1",
            nativeQuery = true)
    void deleteByDeployId(Long deployId);
}
