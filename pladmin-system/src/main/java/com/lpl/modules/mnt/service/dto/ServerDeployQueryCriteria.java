package com.lpl.modules.mnt.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * @author lpl
 * 服务器部署查询条件
 */
@Data
public class ServerDeployQueryCriteria {

    @Query(blurry = "name,ip,account")
    private String blurry;      //根据名称、IP、账户模糊查找

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;
}
