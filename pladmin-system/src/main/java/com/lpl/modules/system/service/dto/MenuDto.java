package com.lpl.modules.system.service.dto;

import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * @author lpl
 * 菜单数据传输对象
 */
@Getter
@Setter
public class MenuDto extends BaseDTO {

    private Long id;    //菜单id

    private String title;   //菜单标题

    private List<MenuDto> children; //子菜单列表

    private Integer type;   //菜单类型（目录、菜单、按钮）

    private String permission;  //菜单权限标识

    private Integer menuSort;   //菜单排序序号

    private String path;    //菜单路由地址

    private String componentName;   //组件名称

    private String component;   //组件地址

    private Long pid;   //上级菜单id

    private Integer subCount;   //子菜单数量

    private Boolean iFrame;    //是否是外链菜单

    private Boolean cache;  //是否缓存

    private Boolean hidden; //是否隐藏

    private String icon;    //图标

    /**
     * 获取是否有子菜单
     */
    public Boolean getHasChildren(){
        return subCount > 0;
    }
    /**
     * 获取是否是叶子节点
     */
    public Boolean getLeaf() {
        return subCount <= 0;
    }
    /**
     * 获取菜单名称
     */
    public String getLabel() {
        return title;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MenuDto menuDto = (MenuDto) o;
        return Objects.equals(id, menuDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
