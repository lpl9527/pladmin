package com.lpl.modules.mnt.service.impl;

import cn.hutool.core.util.IdUtil;
import com.lpl.modules.mnt.domain.DeployHistory;
import com.lpl.modules.mnt.repository.DeployHistoryRepository;
import com.lpl.modules.mnt.service.DeployHistoryService;
import com.lpl.modules.mnt.service.dto.DeployHistoryDto;
import com.lpl.modules.mnt.service.dto.DeployHistoryQueryCriteria;
import com.lpl.modules.mnt.service.mapstruct.DeployHistoryMapper;
import com.lpl.utils.FileUtils;
import com.lpl.utils.PageUtil;
import com.lpl.utils.QueryHelp;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author lpl
 * 部署历史业务实现类
 */
@Service
@RequiredArgsConstructor
public class DeployHistoryServiceImpl implements DeployHistoryService {

    private final DeployHistoryRepository deployHistoryRepository;
    private final DeployHistoryMapper deployHistoryMapper;

    /**
     * 分页查询部署历史
     * @param criteria
     * @param pageable
     */
    @Override
    public Object queryAll(DeployHistoryQueryCriteria criteria, Pageable pageable) {
        Page<DeployHistory> page = deployHistoryRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(deployHistoryMapper::toDto));
    }

    /**
     * 查询部署历史，不分页
     * @param criteria
     */
    @Override
    public List<DeployHistoryDto> queryAll(DeployHistoryQueryCriteria criteria) {
        return deployHistoryMapper.toDto(deployHistoryRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

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

    /**
     * 删除部署历史
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<String> ids) {
        for (String id : ids) {
            deployHistoryRepository.deleteById(id);
        }
    }

    /**
     * 导出部署历史
     * @param queryAll
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<DeployHistoryDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeployHistoryDto deployHistoryDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("部署编号", deployHistoryDto.getDeployId());
            map.put("应用名称", deployHistoryDto.getAppName());
            map.put("部署IP", deployHistoryDto.getIp());
            map.put("部署时间", deployHistoryDto.getDeployDate());
            map.put("部署人员", deployHistoryDto.getDeployUser());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }
}
