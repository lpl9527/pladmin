package com.lpl.modules.mnt.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lpl
 * 部署历史查询条件
 */
@Data
public class DeployHistoryQueryCriteria {

    @Query(blurry = "appName,ip,deployUser")
    private String blurry;

    @Query
    private Long deployId;

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> deployDate;
}
