package com.lpl.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lpl
 * 设置查询常用属性注解
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Query {

    /**
     * 基本对象属性名
     */
    String propName() default "";
    /**
     * 查询方式
     */
    Type type() default Type.EQUAL;
    /**
     * 连接查询的属性名，如User类中的dept
     */
    String joinName() default "";
    /**
     * 连接查询方式，默认左连接
     */
    Join join() default Join.LEFT;
    /**
     * 多字段模糊查询，仅支持String类型字段，多个用逗号隔开, 如@Query(blurry = "email,username")
     */
    String blurry() default "";

    //---------------------------------------------------------
    /**
     * 查询方式枚举
     */
    enum Type {
        //相等
        EQUAL,
        //大于等于
        GREATER_THAN,
        //小于等于
        LESS_THAN,
        //中模糊查询
        INNER_LIKE,
        //左模糊查询
        LEFT_LIKE,
        //右模糊查询
        RIGHT_LIKE,
        //小于
        LESS_THAN_NQ,
        //包含
        IN,
        //不等于
        NOT_EQUAL,
        //在...之间
        BETWEEN,
        //不为空
        NOT_NULL,
        //为空
        IS_NULL
    }

    /**
     * 一个简单的查询方式，可增加其它查询
     */
    enum Join {
        LEFT, RIGHT, INNER
    }
}
