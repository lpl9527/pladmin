package com.lpl.modules.system.service.dto;

import com.lpl.annotation.DataPermission;
import com.lpl.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lpl
 * 部门查询条件
 */
@Data
@DataPermission(fieldName = "id")
public class DeptQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String name;

    @Query
    private Boolean enabled;

    @Query
    private Long pid;

    @Query(type = Query.Type.IS_NULL, propName = "pid")
    private Boolean pidIsNull;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp>createTime;
}
