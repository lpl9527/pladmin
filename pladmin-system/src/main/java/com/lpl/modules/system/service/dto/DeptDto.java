package com.lpl.modules.system.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

/**
 * @author lpl
 * 部门数据传输对象
 */
@Getter
@Setter
public class DeptDto extends BaseDTO {

    private Long id;    //id

    private String name;    //部门名称

    private Boolean enabled;    //是否启用

    private Integer deptSort;       //排序字段

    @JsonInclude(JsonInclude.Include.NON_EMPTY)     //不为空的时候才序列化
    private List<DeptDto> children;     //子部门列表

    private Long pid;   //父部门id

    private Integer subCount;   //子部门数量

    /**
     * 获取是否有子部门
     */
    public Boolean getHasChidren() {
        return subCount > 0;
    }
    /**
     * 获取部门标签
     */
    public String getLabel() {
        return name;
    }

    /**
     * 重写equals()方法
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeptDto deptDto = (DeptDto) o;
        return Objects.equals(id, deptDto.id) &&
                Objects.equals(name, deptDto.name);
    }
    /**
     * 重写hashCode()方法
     */
    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }
}
