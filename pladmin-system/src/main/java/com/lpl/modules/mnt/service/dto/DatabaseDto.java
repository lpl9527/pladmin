package com.lpl.modules.mnt.service.dto;

import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lpl
 * 数据库数据传输对象
 */
@Getter
@Setter
public class DatabaseDto extends BaseDTO {

    /**
     * id
     */
    private String id;

    /**
     * 数据库名称
     */
    private String name;

    /**
     * 数据库连接地址
     */
    private String jdbcUrl;

    /**
     * 数据库密码
     */
    private String pwd;

    /**
     * 用户名
     */
    private String userName;
}
