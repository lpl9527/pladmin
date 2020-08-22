package com.lpl.modules.mnt.service;

import com.lpl.modules.mnt.domain.Deploy;
import com.lpl.modules.mnt.domain.DeployHistory;
import com.lpl.modules.mnt.service.dto.DeployDto;
import com.lpl.modules.mnt.service.dto.DeployQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 部署业务接口
 */
public interface DeployService {

    /**
     * 分页查询所有部署应用
     * @param criteria
     * @param pageable
     */
    Object queryAll(DeployQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部部署
     * @param criteria
     */
    List<DeployDto> queryAll(DeployQueryCriteria criteria);

    /**
     * 根据id查询部署
     * @param id
     */
    DeployDto findById(Long id);

    /**
     * 新增部署
     * @param deploy
     */
    void create(Deploy deploy);

    /**
     * 修改部署
     * @param deploy
     */
    void update(Deploy deploy);

    /**
     * 删除部署
     * @param ids
     */
    void delete(Set<Long> ids);

    /**
     * 导出部署数据
     * @param deployDtos
     * @param response
     * @throws IOException
     */
    void download(List<DeployDto> deployDtos, HttpServletResponse response) throws IOException;

    //--------------------------------------------------------------------------------------4

    /**
     * 部署文件到服务器
     * @param fileSavePath 本地文件路径
     * @param appId     应用id
     */
    void deploy(String fileSavePath, Long appId);

    /**
     * 查询部署状态
     * @param deploy
     */
    String serverStatus(Deploy deploy);

    /**
     * 启动服务
     * @param deploy
     */
    String startServer(Deploy deploy);

    /**
     * 停止服务
     * @param deploy
     */
    String stopServer(Deploy deploy);

    /**
     * 系统还原
     * @param deployHistory
     */
    String serverReduction(DeployHistory deployHistory);
}
