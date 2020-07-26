package com.lpl.base;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.Column;
import javax.persistence.EntityListeners;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.sql.Timestamp;

/**
 * @author lpl
 * 基础数据访问对象实体
 */
@Getter
@Setter
@MappedSuperclass   //标注了此注解的实体类不会被映射到数据库表中，但是其属性都会被映射到其子类的数据库字段中
@EntityListeners(AuditingEntityListener.class)      //监听属性的变化。支持在字段或者方法上进行注解@CreatedDate、@CreatedBy、@LastModifiedDate、@LastModifiedBy
public class BaseEntity implements Serializable {

    @CreatedBy  //表示该字段为创建人，在这个实体被insert时会被设置值
    @Column(name = "create_by", updatable = false)  //该字段不可更新
    @ApiModelProperty(value = "创建人", hidden = true)
    private String createBy;

    @LastModifiedBy  //表示该字段为最终修改人，在实体被update时更新值
    @Column(name = "update_by")
    @ApiModelProperty(value = "更新人", hidden = true)
    private String updateBy;

    @CreationTimestamp  //该注解可在插入时对属性对应的日期类型创建默认值
    @Column(name = "create_time", updatable = false)
    @ApiModelProperty(value = "创建时间", hidden = true)
    private Timestamp createTime;

    @UpdateTimestamp    //更新时以当前时间为默认值
    @Column(name = "update_time")
    @ApiModelProperty(value = "更新时间", hidden = true)
    private Timestamp updateTime;

    /**
     * 分组校验
     */
    public @interface Create{

    }
    public @interface Update{

    }
    /**
     * 重写toString
     */
    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        Field[] fields = this.getClass().getDeclaredFields();
        try {
            for (Field f : fields) {
                f.setAccessible(true);
                builder.append(f.getName(), f.get(this)).append("\n");
            }
        } catch (Exception e) {
            builder.append("toString()方法发生错误！");
        }
        return builder.toString();
    }
}
