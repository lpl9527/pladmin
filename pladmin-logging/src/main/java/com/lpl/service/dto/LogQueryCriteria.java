package com.lpl.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lpl
 * 日志查询类
 */
@Data
public class LogQueryCriteria {

    @Query(blurry = "username,description,address,requestIp,method,params")
    private String blurry;      //多字段模糊查询

    @Query
    private String logType;     //日志类型

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;   //创建时间
}
