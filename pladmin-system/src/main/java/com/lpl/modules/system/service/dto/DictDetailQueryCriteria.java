package com.lpl.modules.system.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

/**
 * @author lpl
 * 数据字典查询条件
 */
@Data
public class DictDetailQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String label;

    @Query(propName = "name", joinName = "dict")
    private String dictName;
}
