package com.lpl.utils;

/**
 * redis缓存的Key的集合
 */
public interface CacheKey {

    /**
     *  用户、应用、岗位、菜单、角色、部门相关key
     */
    String USER_MODIFY_TIME_KEY = "user:modify:time:key:";
    String APP_MODIFY_TIME_KEY = "app:modify:time:key:";
    String JOB_MODIFY_TIME_KEY = "job:modify:time:key:";
    String MENU_MODIFY_TIME_KEY = "menu:modify:time:key:";
    String ROLE_MODIFY_TIME_KEY = "role:modify:time:key:";
    String DEPT_MODIFY_TIME_KEY = "dept:modify:time:key:";

    /**
     * 用户
     */
    String USER_ID = "user::id:";
    String USER_NAME = "user::username:";
    /**
     * 数据
     */
    String DATA_USER = "data::user:";
    /**
     * 菜单
     */
    String MENU_USER = "menu::user:";
    /**
     * 角色授权
     */
    String ROLE_AUTH = "role::auth:";
    /**
     * 角色信息
     */
    String ROLE_ID = "role::id:";
}
