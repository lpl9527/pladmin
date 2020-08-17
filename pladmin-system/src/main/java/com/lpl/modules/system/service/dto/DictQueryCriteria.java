package com.lpl.modules.system.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

/**
 * @author lpl
 * 字典查询条件
 */
@Data
public class DictQueryCriteria {

    @Query(blurry = "name,description")
    private String blurry;

}
