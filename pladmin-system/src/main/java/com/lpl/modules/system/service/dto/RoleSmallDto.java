package com.lpl.modules.system.service.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author lpl
 * 角色部分属性数据传输对象
 */
@Data
public class RoleSmallDto implements Serializable {

    /**
     * id
     */
    private Long id;
    /**
     * 角色名称
     */
    private String name;
    /**
     * 角色级别
     */
    private Integer level;
    /**
     * 数据范围
     */
    private String dataScope;

}
