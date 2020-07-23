package com.lpl.modules.system.service.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author lpl
 * 职位部分属性数据传输对象
 */
@Data
@NoArgsConstructor
public class JobSmallDto {

    /**
     * id
     */
    private Long id;
    /**
     * 职位名称
     */
    private String name;
}
