package com.lpl.modules.mnt.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lpl
 * 应用查询条件
 */
@Data
public class AppQueryCriteria {

    @Query(type = Query.Type.INNER_LIKE)
    private String name;   //应用名称模糊查询

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
