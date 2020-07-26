package com.lpl.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lpl
 * 数据权限枚举
 */
@Getter
@AllArgsConstructor
public enum DataScopeEnum {

    /**
     * 全部的数据权限
     */
    ALL("全部", "全部的数据权限"),
    /**
     * 所在部门的数据权限
     */
    THIS_LEVEL("本级", "所在部门的数据权限"),
    /**
     * 自定义的数据权限
     */
    CUSTOMIZE("自定义", "自定义的数据权限");

    private final String value;     //值
    private final String description;   //描述

    /**
     * 根据数据权限的value获取数据权限枚举
     * @param value
     */
    public static DataScopeEnum find(String value) {
        for (DataScopeEnum dataScopeEnum : DataScopeEnum.values()) {
            if (value.equals(dataScopeEnum.getValue())) {
                return dataScopeEnum;
            }
        }
        return null;
    }
}
