package com.lpl.base;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Timestamp;

/**
 * @author lpl
 * 基础数据传输对象
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
