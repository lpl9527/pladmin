package com.lpl.modules.mnt.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author lpl
 * 部署历史实体类
 */
@Getter
@Setter
@Entity
@Table(name = "mnt_deploy_history")
public class DeployHistory implements Serializable {

    @Id
    @Column(name = "history_id")
    @ApiModelProperty(value = "部署历史ID", hidden = true)
    private String id;

    @ApiModelProperty(value = "应用名称")
    private String appName;

    @ApiModelProperty(value = "IP")
    private String ip;

    @CreationTimestamp
    @ApiModelProperty(value = "部署时间")
    private Timestamp deployDate;

    @ApiModelProperty(value = "部署者")
    private String deployUser;

    @ApiModelProperty(value = "部署ID")
    private Long deployId;

    public void copy(DeployHistory source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
