package com.lpl.modules.mnt.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.lpl.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

/**
 * @author lpl
 * 部署应用实体类
 */
@Getter
@Setter
@Entity
@Table(name = "mnt_deploy")
public class Deploy extends BaseEntity {

    @Id
    @Column(name = "deploy_id")
    @ApiModelProperty(value = "部署ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany
    @ApiModelProperty(name = "服务器", hidden = true)
    @JoinTable(name = "mnt_deploy_server",
            joinColumns = {@JoinColumn(name = "deploy_id",referencedColumnName = "deploy_id")},
            inverseJoinColumns = {@JoinColumn(name = "server_id",referencedColumnName = "server_id")})
    private Set<ServerDeploy> deploys;

    @ManyToOne
    @JoinColumn(name = "app_id")
    @ApiModelProperty(value = "应用编号")
    private App app;

    public void copy(Deploy source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
