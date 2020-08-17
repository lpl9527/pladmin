package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.Dict;
import com.lpl.modules.system.service.dto.DictDto;
import com.lpl.modules.system.service.dto.DictQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author lpl
 * 字典业务接口
 */
public interface DictService {

    /**
     * 查询全部字典数据
     * @param criteria
     */
    List<DictDto> queryAll(DictQueryCriteria criteria);

    /**
     * 分页查询字典数据
     * @param criteria
     * @param pageable
     */
    Map<String, Object> queryAll(DictQueryCriteria criteria, Pageable pageable);

    /**
     * 新增字典
     * @param dict
     */
    void create(Dict dict);

    /**
     * 更新字典
     * @param dict
     */
    void update(Dict dict);

    /**
     * 批量删除字典
     * @param ids
     */
    void delete(Set<Long> ids);

    /**
     * 导出字典数据
     * @param dictDtos
     * @param response
     * @throws IOException
     */
    void download(List<DictDto> dictDtos, HttpServletResponse response) throws IOException;
}
