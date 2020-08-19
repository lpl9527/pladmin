package com.lpl.modules.mnt.domain;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import com.lpl.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Objects;

/**
 * @author lpl
 * 服务器部署实体类
 */
@Entity
@Getter
@Setter
@Table(name="mnt_server")
public class ServerDeploy extends BaseEntity {

    @Id
    @Column(name = "server_id")
    @ApiModelProperty(value = "服务器ID", hidden = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty(value = "服务器名称")
    private String name;

    @ApiModelProperty(value = "IP")
    private String ip;

    @ApiModelProperty(value = "端口")
    private Integer port;

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "密码")
    private String password;

    public void copy(ServerDeploy source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerDeploy that = (ServerDeploy) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
