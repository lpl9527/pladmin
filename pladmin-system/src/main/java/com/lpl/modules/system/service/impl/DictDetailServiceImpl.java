package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.domain.Dict;
import com.lpl.modules.system.domain.DictDetail;
import com.lpl.modules.system.mapstruct.DictDetailMapper;
import com.lpl.modules.system.repository.DictDetailRepository;
import com.lpl.modules.system.repository.DictRepository;
import com.lpl.modules.system.service.DictDetailService;
import com.lpl.modules.system.service.dto.DictDetailDto;
import com.lpl.modules.system.service.dto.DictDetailQueryCriteria;
import com.lpl.utils.PageUtil;
import com.lpl.utils.QueryHelp;
import com.lpl.utils.RedisUtils;
import com.lpl.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final DictRepository dictRepository;
    private final RedisUtils redisUtils;

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

    /**
     * 创建字典详情
     * @param dictDetail
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(DictDetail dictDetail) {
        //保存
        dictDetailRepository.save(dictDetail);
        //清理缓存
        delCaches(dictDetail);
    }

    /**
     * 更新字典详情
     * @param dictDetail
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(DictDetail dictDetail) {
        //查询字典详情
        DictDetail dictDetailOld = dictDetailRepository.findById(dictDetail.getId()).orElseGet(DictDetail::new);
        ValidationUtil.isNull( dictDetailOld.getId(),"DictDetail","id",dictDetail.getId());
        dictDetail.setId(dictDetailOld.getId());
        //保存
        dictDetailRepository.save(dictDetail);
        // 清理缓存
        delCaches(dictDetail);
    }

    /**
     * 删除字典详情
     * @param id
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        //查询
        DictDetail dictDetail = dictDetailRepository.findById(id).orElseGet(DictDetail::new);
        // 清理缓存
        delCaches(dictDetail);
        //删除
        dictDetailRepository.deleteById(id);
    }

    /**
     * 清理缓存
     * @param dictDetail
     */
    public void delCaches(DictDetail dictDetail){
        Dict dict = dictRepository.findById(dictDetail.getDict().getId()).orElseGet(Dict::new);
        redisUtils.del("dept::name:" + dict.getName());
    }
}
