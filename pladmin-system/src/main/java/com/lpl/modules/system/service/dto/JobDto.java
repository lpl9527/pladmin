package com.lpl.modules.system.service.dto;

import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lpl
 * 岗位数据传输对象
 */
@Getter
@Setter
@NoArgsConstructor
public class JobDto extends BaseDTO {

    private Long id;    //岗位id

    private String name;    //岗位名称

    private Boolean enabled;    //是否启用

    private Integer jobSort;    //岗位排序字段

    public JobDto(String name, Boolean enabled) {
        this.name = name;
        this.enabled = enabled;
    }
}
