package com.lpl.modules.system.domain;

import com.lpl.base.BaseEntity;
import com.sun.org.apache.xpath.internal.operations.Bool;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author lpl
 * 岗位数据访问对象
 */
@Getter
@Setter
@Entity
@Table(name = "sys_job")
public class Job extends BaseEntity {

    @Id
    @Column(name = "job_id")
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "岗位id", hidden = true)
    private Long id;

    @NotBlank
    @ApiModelProperty(value = "岗位名称")
    private String name;

    @NotNull
    @ApiModelProperty(value = "岗位排序序号")
    private Long jobSort;

    @NotNull
    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;

    /**
     * 重写equals()方法
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {    //内存地址相同，肯定为同一对象，返回true
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) { //Class对象不同，可定不相等（无论是值还是地址）
            return false;
        }
        Job job = (Job) o;
        return Objects.equals(id, job.id);     //id相同时才认为两岗位对象相等
    }
    /**
     * 重写hashCode()方法
     */
    @Override
    public int hashCode(){
        return Objects.hash(this.id);
    }
}
