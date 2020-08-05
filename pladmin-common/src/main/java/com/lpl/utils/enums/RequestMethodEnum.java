package com.lpl.utils.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lpl
 * 请求方式枚举
 */
@Getter
@AllArgsConstructor
public enum RequestMethodEnum {

    /**
     * 搜寻 @AnonymousGetMapping
     */
    GET("GET"),
    /**
     * 搜寻 @AnonymousPostMapping
     */
    POST("POST"),
    /**
     * 搜寻 @AnonymousPutMapping
     */
    PUT("PUT"),
    /**
     * 搜寻 @AnonymousPatchMapping
     */
    PATCH("PATCH"),
    /**
     * 搜寻 @AnonymousDeleteMapping
     */
    DELETE("DELETE"),
    /**
     * 所有 Request 接口都放行，与请求方式无关
     */
    ALL("All");

    /**
     * Request请求类型
     */
    private final String type;

    /**
     * 根据指定类型查找对应类型枚举，找不到就返回所有类型
     * @param type
     */
    public static RequestMethodEnum find(String type) {
        for (RequestMethodEnum requestMethod : RequestMethodEnum.values()) {
            if (type.equals(requestMethod.getType())) {
                return requestMethod;
            }
        }
        //找不到就返回所有类型
        return ALL;
    }
}
