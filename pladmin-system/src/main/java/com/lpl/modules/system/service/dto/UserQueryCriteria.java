package com.lpl.modules.system.service.dto;

import com.lpl.annotation.Query;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 用户查询条件
 */
@Data
public class UserQueryCriteria implements Serializable {

    @Query
    private Long id;    //用户id

    private Long deptId;    //查询条件部门id

    @Query(propName = "id", type = Query.Type.IN, joinName = "dept")
    private Set<Long> deptIds = new HashSet<>();    //用于存放要查询的部门id及当前用户具有访问权限的部门id集合

    @Query(blurry = "email,username,nickName")      //特别注意：多个字段之间不能有空格
    private String blurry;  //查询条件匹配的字段

    @Query
    private Boolean enabled;    //是否可用

    @Query(type = Query.Type.BETWEEN)
    private List<Timestamp> createTime;     //创建时间
}
