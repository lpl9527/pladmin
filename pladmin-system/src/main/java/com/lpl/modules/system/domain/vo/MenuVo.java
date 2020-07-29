package com.lpl.modules.system.domain.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author lpl
 * 菜单视图对象，构建前端路由时用到
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MenuVo implements Serializable {

    private String name;    //路由组件名称

    private String path;    //路由地址

    private Boolean hidden; //是否隐藏

    private String redirect;    //重定向地址

    private String component;   //路由地址

    private Boolean alwaysShow; //总是展示

    private MenuMetaVo  meta;   //菜单信息元数据

    private List<MenuVo> children;  //子菜单列表
}
