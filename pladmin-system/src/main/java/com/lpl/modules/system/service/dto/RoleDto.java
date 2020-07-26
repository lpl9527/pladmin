package com.lpl.modules.system.service.dto;

import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

/**
 * @author lpl
 * 角色数据传输对象
 */
@Getter
@Setter
public class RoleDto extends BaseDTO {

    private Long id;    //角色id

    private String name;  //角色名称

    private Set<MenuDto> menus;     //菜单

    private Set<DeptDto> depts;     //部门

    private String dataScope;   //数据访问权限

    private Integer level;  //角色级别，数值越低，级别越高

    private String description; //角色描述

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RoleDto roleDto = (RoleDto) o;
        return Objects.equals(id, roleDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}
