package com.lpl.modules.mnt.service;

import com.lpl.modules.mnt.domain.DeployHistory;
import com.lpl.modules.mnt.service.dto.DeployHistoryDto;
import com.lpl.modules.mnt.service.dto.DeployHistoryQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 部署历史业务接口
 */
public interface DeployHistoryService {

    /**
     * 分页查询部署历史
     * @param criteria
     * @param pageable
     */
    Object queryAll(DeployHistoryQueryCriteria criteria, Pageable pageable);

    /**
     * 查询部署历史，不分页
     * @param criteria
     */
    List<DeployHistoryDto> queryAll(DeployHistoryQueryCriteria criteria);

    /**
     * 创建部署历史
     * @param deployHistory
     */
    void create(DeployHistory deployHistory);

    /**
     * 删除部署历史
     * @param ids
     */
    void delete(Set<String> ids);

    /**
     * 导出部署历史
     * @param queryAll
     * @param response
     * @throws IOException
     */
    void download(List<DeployHistoryDto> queryAll, HttpServletResponse response) throws IOException;
}
