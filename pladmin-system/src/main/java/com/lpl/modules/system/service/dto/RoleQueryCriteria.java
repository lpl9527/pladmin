package com.lpl.modules.system.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lpl
 * 角色查询条件
 */
@Data
public class RoleQueryCriteria {

    @Query(blurry = "name,description")
    private String blurry;      //模糊查询条件字段

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;     //创建时间范围查询
}
