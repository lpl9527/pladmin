package com.lpl.modules.mnt.service;

import com.lpl.modules.mnt.domain.DeployHistory;

/**
 * @author lpl
 * 部署历史业务接口
 */
public interface DeployHistoryService {

    /**
     * 创建部署历史
     * @param deployHistory
     */
    void create(DeployHistory deployHistory);
}
