package com.lpl.modules.mnt.repository;

import com.lpl.modules.mnt.domain.ServerDeploy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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
}
