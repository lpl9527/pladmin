package com.lpl.modules.system.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lpl.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;

/**
 * 部门数据访问对象
 */
@Getter
@Setter
@Entity
@Table(name = "sys_dept")
public class Dept extends BaseEntity {

    @Id
    @Column(name = "dept_id")
    @NotNull(groups = Update.class)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @ApiModelProperty(value = "部门id", hidden = true)
    private Long id;

    @JsonIgnore
    @ManyToMany(mappedBy = "depts")
    @ApiModelProperty(value = "部门角色")
    private Set<Role> roles;

    @NotBlank
    @ApiModelProperty(value = "部门名称")
    private String name;

    @ApiModelProperty(value = "部门上级id")
    private Long pid;

    @NotNull
    @ApiModelProperty(value = "部门是否启用")
    private Boolean enabled;

    @ApiModelProperty(value = "部门排序序号")
    private Integer deptSort;

    @ApiModelProperty(value = "部门子节点数目", hidden = true)
    private Integer subCount = 0;

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
        Dept dept = (Dept) o;
        return Objects.equals(id, dept.id);     //id相同时才认为两部门对象相等
    }
    /**
     * 重写hashCode()方法
     */
    @Override
    public int hashCode(){
        return Objects.hash(this.id);
    }
}
