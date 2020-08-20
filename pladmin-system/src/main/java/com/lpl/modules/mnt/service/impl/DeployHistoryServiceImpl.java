package com.lpl.modules.mnt.service.impl;

import cn.hutool.core.util.IdUtil;
import com.lpl.modules.mnt.domain.DeployHistory;
import com.lpl.modules.mnt.repository.DeployHistoryRepository;
import com.lpl.modules.mnt.service.DeployHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author lpl
 * 部署历史业务实现类
 */
@Service
@RequiredArgsConstructor
public class DeployHistoryServiceImpl implements DeployHistoryService {

    private final DeployHistoryRepository deployHistoryRepository;

    /**
     * 创建部署历史
     * @param deployHistory
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DeployHistory deployHistory) {
        deployHistory.setId(IdUtil.simpleUUID());
        deployHistoryRepository.save(deployHistory);
    }
}
