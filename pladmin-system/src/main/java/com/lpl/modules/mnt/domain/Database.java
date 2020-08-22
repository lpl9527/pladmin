package com.lpl.modules.mnt.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.lpl.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author lpl
 * 数据库实体类
 */
@Getter
@Setter
@Entity
@Table(name = "mnt_database")
public class Database extends BaseEntity {

    @Id
    @Column(name = "db_id")
    @ApiModelProperty(value = "数据库ID", hidden = true)
    private String id;

    @ApiModelProperty(value = "数据库名称")
    private String name;

    @ApiModelProperty(value = "数据库连接地址")
    private String jdbcUrl;

    @ApiModelProperty(value = "数据库密码")
    private String pwd;

    @ApiModelProperty(value = "用户名")
    private String userName;

    public void copy(Database source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
