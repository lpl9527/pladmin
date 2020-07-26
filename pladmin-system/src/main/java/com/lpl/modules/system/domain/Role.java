package com.lpl.modules.system.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lpl.base.BaseEntity;
import com.lpl.utils.enums.DataScopeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

/**
 * @author lpl
 * 角色数据访问对象
 */
@Getter
@Setter
@Entity
@Table(name = "sys_role")
public class Role extends BaseEntity {

    @Id
    @Column(name = "role_id")
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "角色id", hidden = true)
    private Long id;

    @JsonIgnore
    @ManyToMany(mappedBy = "roles")     //可以从用户方维护角色，但不可以从角色方维护用户
    @ApiModelProperty(value = "用户", hidden = true)
    private Set<User> users;    //与用户多对多关联

    @ManyToMany
    @JoinTable(name = "sys_roles_menus",
                joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "role_id")},
                inverseJoinColumns = {@JoinColumn(name = "menu_id", referencedColumnName = "menu_id")})
    @ApiModelProperty(value = "菜单", hidden = true)
    private Set<Menu> menus;    //角色方来维护菜单

    @ManyToMany
    @JoinTable(name = "sys_roles_depts",
                joinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "role_id")},
                inverseJoinColumns = {@JoinColumn(name = "dept_id", referencedColumnName = "dept_id")})
    @ApiModelProperty(value = "部门", hidden = true)
    private Set<Dept> depts;    //角色方来维护部门

    @NotBlank
    @ApiModelProperty(value = "角色名称", hidden = true)
    private String name;

    @ApiModelProperty(value = "角色描述")
    private String description;

    @Column(name = "level")
    @ApiModelProperty(value = "角色级别（数值越小，级别越高）")
    private Integer level = 3;

    @ApiModelProperty(value = "数据权限（包括：全部、本级、自定义）")
    private String dataScope = DataScopeEnum.THIS_LEVEL.getValue();

    /**
     * 重写equals()方法
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {    //内存地址相同，肯定为同一对象，返回true
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) { //Class对象不同，可定不相等（无论是值还是地址）
            return false;
        }
        Role role = (Role) o;
        return Objects.equals(id, role.id);     //id相同时才认为两角色对象相等
    }
    /**
     * 重写hashCode()方法
     */
    @Override
    public int hashCode(){
        return Objects.hash(this.id);
    }
}
