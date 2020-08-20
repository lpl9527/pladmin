package com.lpl.modules.mnt.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lpl
 * 部署查询条件
 */
@Data
public class DeployQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE, propName = "name", joinName = "app")
    private String appName;     //根据应用名称模糊查找应用

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
