package com.lpl.base;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Timestamp;

/**
 * @author lpl
 * 定义基础数据传输对象
 *
 * VO（view object）：表现层对象代表展示层需要显示的数据。
 * DTO（Data Transfer Object）： 数据传输对象，可用于放置一些不需要与数据库交互或者定制化放置一个或多个实体类的字段。用于客户端与服务端进行跨进程或远程数据传输。
 * PO（persistent object）：持久对象，对应数据库中的entity，可以简单认为一个PO对应数据库中的一条记录。
 * DAO（data access object）：数据访问对象，用于与数据库进行数据交互，封装对数据库的访问。
 *
 * POJO（plain ordinary java object）：无规则简单java对象，一个中间对象，可以转化为PO、DTO、VO。
 */
@Getter
@Setter
public class BaseDTO implements Serializable {

    private String createBy;    //创建人员

    private String updateBy;    //更新人员

    private Timestamp createTime;   //创建时间

    private Timestamp updateTime;   //更新时间

    /**
     * 使用反射重写toString()方法，子类都不需要再次重写
     */
    @Override
    public String toString() {
        //创建String构建对象
        ToStringBuilder builder = new ToStringBuilder(this);
        //反射获取所有Field对象数组
        Field[] fields = this.getClass().getDeclaredFields();
        try {
            for (Field field : fields){
                field.setAccessible(true);  //暴力反射
                builder.append(field.getName(), field.get(this)).append("\n");
            }
        }catch (Exception e) {
            builder.append("toString builder occur an error!");
        }
        return builder.toString();
    }
}
