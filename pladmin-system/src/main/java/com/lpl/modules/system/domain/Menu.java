package com.lpl.modules.system.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lpl.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

/**
 * @author lpl
 * 菜单数据访问对象
 */
@Getter
@Setter
@Entity
@Table(name = "sys_menu")
public class Menu extends BaseEntity {

    @Id
    @Column(name = "menu_id")
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "菜单id", hidden = true)
    private Long id;

    @JsonIgnore //不希望传递给前台的一些字段用此注解标注
    @ManyToMany(mappedBy = "menus")     //此属性只能由角色方维护
    @ApiModelProperty(value = "菜单角色")
    private Set<Role> roles;

    @ApiModelProperty(value = "上级菜单id")
    private Long pid;

    @ApiModelProperty(value = "菜单标题")
    private String title;

    @Column(name = "name")      //指定了此注解的属性可以对数据表中字段重命名，不指定的属性也作为数据表字段存在
    private String componentName;

    @ApiModelProperty(value = "菜单组件路径")
    private String component;

    @ApiModelProperty(value = "菜单路由地址")
    private String path;

    @ApiModelProperty(value = "菜单排序序号")
    private Integer menuSort = 999;

    @ApiModelProperty(value = "菜单类型（目录-0、菜单-1、按钮-2）")
    private Integer type;

    @ApiModelProperty(value = "菜单权限标识")
    private String permission;

    @ApiModelProperty(value = "菜单图标")
    private String icon;

    @Column(columnDefinition = "bit(1) default 0")      //指定数据类型和默认值
    @ApiModelProperty(value = "菜单缓存")
    private Boolean cache;

    @Column(columnDefinition = "bit(1) default 0")
    @ApiModelProperty(value = "菜单是否隐藏")
    private Boolean hidden;

    @ApiModelProperty(value = "菜单子节点数目", hidden = true)
    private Integer subCount = 0;

    @ApiModelProperty(value = "是否是外链菜单")
    private Boolean iFrame;

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
        Menu menu = (Menu)o;
        return Objects.equals(id, menu.id);     //id相同时才认为两菜单对象相等
    }
    /**
     * 重写hashCode()方法
     */
    @Override
    public int hashCode(){
        return Objects.hash(this.id);
    }
}
