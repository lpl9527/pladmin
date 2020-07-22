package com.lpl.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author lpl
 * Spring工具类
 */
@Slf4j
public class SpringContextHolder implements ApplicationContextAware, DisposableBean {

    private static ApplicationContext applicationContext = null;    //spring上下文

    /**
     * 根据类名从spring上下文中取得Bean, 自动转型为所赋值对象的类型
     */
    public static <T> T getBean(String name) {
        assertContextInjected();
        return (T) applicationContext.getBean(name);
    }
    /**
     * 根据类名从spring上下文中取得Bean, 自动转型为所赋值对象的类型
     */
    public static <T> T getBean(Class<T> requiredType) {
        assertContextInjected();
        return applicationContext.getBean(requiredType);
    }
    /**
     * 检查ApplicationContext不为空，为空抛出异常
     */
    private static void assertContextInjected() {
        if (applicationContext == null) {
            throw new IllegalStateException("applicaitonContext属性未注入, 请在applicationContext" +
                    ".xml中定义SpringContextHolder或在SpringBoot启动类中注册SpringContextHolder.");
        }
    }
    /**
     * 清除SpringContextHolder中的ApplicationContext为空
     */
    private static void clearHolder() {
        log.debug("清除SpringContextHolder中的ApplicationContext:"
                + applicationContext);
        applicationContext = null;
    }

    /**
     * 在Bean销毁时清理掉其中的spring上下文对象
     */
    @Override
    public void destroy(){
        SpringContextHolder.clearHolder();
    }
    /**
     * 设置spring上下文对象。spring能够自动执行setApplicationContext()方法初始化上下文
     * @param applicationContext
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringContextHolder.applicationContext != null) {
            log.warn("SpringContextHolder中的ApplicationContext被覆盖, 原有ApplicationContext为:" + SpringContextHolder.applicationContext);
        }
        SpringContextHolder.applicationContext = applicationContext;
    }
}
