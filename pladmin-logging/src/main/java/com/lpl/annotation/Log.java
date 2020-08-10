package com.lpl.annotation;

import com.lpl.annotation.type.LogActionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lpl
 * 日志注解，用于记录用户操作
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {

    String value() default "";  //用户操作

    boolean enable() default true;  //是否启用，默认true

    LogActionType type() default LogActionType.SELECT;  //操作类型，默认查询
}
