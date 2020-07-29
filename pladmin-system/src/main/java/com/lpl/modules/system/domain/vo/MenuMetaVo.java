package com.lpl.modules.system.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author lpl
 * 菜单基础视图对象
 */
@Data
@AllArgsConstructor
public class MenuMetaVo implements Serializable {

    private String title;   //菜单标题

    private String icon;    //菜单图标

    private Boolean noCache;    //是否缓存
}
