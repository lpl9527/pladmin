package com.lpl.modules.mnt.service;

import com.lpl.modules.mnt.domain.ServerDeploy;
import com.lpl.modules.mnt.service.dto.ServerDeployDto;
import com.lpl.modules.mnt.service.dto.ServerDeployQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 服务器部署业务接口
 */
public interface ServerDeployService {

    /**
     * 分页查询全部服务器
     * @param criteria
     */
    Object queryAll(ServerDeployQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部服务器
     * @param criteria
     */
    List<ServerDeployDto> queryAll(ServerDeployQueryCriteria criteria);

    /**
     * 新增服务器
     * @param serverDeploy
     */
    void create(ServerDeploy serverDeploy);

    /**
     * 编辑服务器
     * @param serverDeploy
     */
    void update(ServerDeploy serverDeploy);

    /**
     * 删除服务器
     * @param ids
     */
    void delete(Set<Long> ids);

    /**
     * 测试连接服务器
     * @param serverDeploy
     */
    Boolean testConnect(ServerDeploy serverDeploy);

    /**
     * 导出服务器数据
     * @param queryAll
     * @param response
     * @throws IOException
     */
    void download(List<ServerDeployDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 根据ip查询服务器信息
     * @param ip
     */
    ServerDeployDto findByIp(String ip);
}
