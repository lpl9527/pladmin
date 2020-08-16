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
    private String blurry;      //多字段模糊查询（这种多字段模糊查询的方式会查询到其它用户的操作日志）

    @Query(propName = "username", type = Query.Type.EQUAL)
    private String username;    //根据用户名来查询，查询方式为相等

    @Query
    private String logType;     //日志类型

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;   //创建时间
}
