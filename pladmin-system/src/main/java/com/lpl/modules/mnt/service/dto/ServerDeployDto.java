package com.lpl.modules.mnt.service.dto;

import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

/**
 * @author lpl
 * 服务器部署数据传输对象
 */
@Getter
@Setter
public class ServerDeployDto extends BaseDTO {

    private Long id;

    private String name;

    private String ip;

    private Integer port;

    private String account;

    private String password;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ServerDeployDto that = (ServerDeployDto) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
