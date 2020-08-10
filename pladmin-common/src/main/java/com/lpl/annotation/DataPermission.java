package com.lpl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lpl
 * 用于判断是否过滤数据权限
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataPermission {

    /**
     * Entity 中的字段名称
     */
    String fieldName() default "";

    /**
     * Entity 中与部门关联的字段名称
     */
    String joinName() default "";
}
