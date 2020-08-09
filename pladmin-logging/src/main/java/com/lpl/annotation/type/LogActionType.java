package com.lpl.annotation.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lpl
 * 日志操作类型枚举
 */
public enum LogActionType {

    /**
     * 操作类型：增删改查
     */
    ADD("新增"),
    SELECT("查询"),
    UPDATE("更新"),
    DELETE("删除");

    private String value;

    LogActionType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
