package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.domain.DictDetail;
import com.lpl.modules.system.mapstruct.DictDetailMapper;
import com.lpl.modules.system.repository.DictDetailRepository;
import com.lpl.modules.system.service.DictDetailService;
import com.lpl.modules.system.service.dto.DictDetailDto;
import com.lpl.modules.system.service.dto.DictDetailQueryCriteria;
import com.lpl.utils.PageUtil;
import com.lpl.utils.QueryHelp;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @author lpl
 * 数据字典详情业务层实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dict")
public class DictDetailServiceImpl implements DictDetailService {

    private final DictDetailRepository dictDetailRepository;
    private final DictDetailMapper dictDetailMapper;

    /**
     * 分页查询数据字典详情
     * @param criteria 查询条件
     * @param pageable 分页参数
     */
    @Override
    public Map<String, Object> queryAll(DictDetailQueryCriteria criteria, Pageable pageable) {
        Page<DictDetail> page = dictDetailRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(dictDetailMapper::toDto));
    }
}
