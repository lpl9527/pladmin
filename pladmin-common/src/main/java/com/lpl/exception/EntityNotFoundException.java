package com.lpl.exception;

import org.springframework.util.StringUtils;

/**
 * @author lpl
 * 自定义未发现实体运行时异常
 */
public class EntityNotFoundException extends RuntimeException{

    /**
     * 抛出运行时异常消息体
     * @param clazz 对象Class
     * @param field 属性
     * @param val   属性值
     */
    public EntityNotFoundException(Class clazz, String field, String val) {
        super(EntityNotFoundException.generateMessage(clazz.getSimpleName(), field, val));
    }

    private static String generateMessage(String entity, String field, String val) {
        return "实体：" + StringUtils.capitalize(entity) + "的属性：" + field + "和值" + val + "不存在！";
    }
}
