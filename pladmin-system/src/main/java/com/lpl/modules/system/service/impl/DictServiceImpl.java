package com.lpl.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.lpl.modules.system.domain.Dict;
import com.lpl.modules.system.mapstruct.DictMapper;
import com.lpl.modules.system.repository.DictRepository;
import com.lpl.modules.system.service.DictService;
import com.lpl.modules.system.service.dto.DictDetailDto;
import com.lpl.modules.system.service.dto.DictDto;
import com.lpl.modules.system.service.dto.DictQueryCriteria;
import com.lpl.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author lpl
 * 字典业务实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dict")
public class DictServiceImpl implements DictService {

    private final DictMapper dictMapper;
    private final DictRepository dictRepository;
    private final RedisUtils redisUtils;

    /**
     * 查询全部字典数据
     * @param criteria
     */
    @Override
    public List<DictDto> queryAll(DictQueryCriteria criteria) {
        List<Dict> list = dictRepository.findAll((root, query, cb) -> QueryHelp.getPredicate(root, criteria, cb));
        return dictMapper.toDto(list);
    }

    /**
     * 分页查询字典数据
     * @param criteria
     * @param pageable
     */
    @Override
    public Map<String, Object> queryAll(DictQueryCriteria criteria, Pageable pageable) {
        Page<Dict> page = dictRepository.findAll((root, query, cb) -> QueryHelp.getPredicate(root, criteria, cb), pageable);
        return PageUtil.toPage(page.map(dictMapper::toDto));
    }

    /**
     * 新增字典
     * @param dict
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Dict dict) {
        dictRepository.save(dict);
    }

    /**
     * 更新字典
     * @param dict
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dict dict) {
       //清理缓存
        delCaches(dict);
        //根据id查询字典
        Dict oldDict = dictRepository.findById(dict.getId()).orElseGet(Dict::new);
        ValidationUtil.isNull( dict.getId(),"Dict","id",dict.getId());
        dict.setId(oldDict.getId());
        //保存
        dictRepository.save(dict);
    }

    /**
     * 批量删除字典
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        //查询出字典清理缓存
        List<Dict> dicts = dictRepository.findByIdIn(ids);
        for (Dict dict : dicts) {
            delCaches(dict);
        }
        //批量删除
        dictRepository.deleteByIdIn(ids);
    }

    /**
     * 导出字典数据
     * @param dictDtos
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<DictDto> dictDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DictDto dictDto : dictDtos) {
            if(CollectionUtil.isNotEmpty(dictDto.getDictDetails())){
                //字典详情
                for (DictDetailDto dictDetail : dictDto.getDictDetails()) {
                    Map<String,Object> map = new LinkedHashMap<>();
                    map.put("字典名称", dictDto.getName());
                    map.put("字典描述", dictDto.getDescription());
                    map.put("字典标签", dictDetail.getLabel());
                    map.put("字典值", dictDetail.getValue());
                    map.put("创建日期", dictDetail.getCreateTime());
                    list.add(map);
                }
            } else {
                Map<String,Object> map = new LinkedHashMap<>();
                map.put("字典名称", dictDto.getName());
                map.put("字典描述", dictDto.getDescription());
                map.put("字典标签", null);
                map.put("字典值", null);
                map.put("创建日期", dictDto.getCreateTime());
                list.add(map);
            }
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 清理缓存
     * @param dict
     */
    public void delCaches(Dict dict){
        redisUtils.del("dept::name:" + dict.getName());
    }
}
