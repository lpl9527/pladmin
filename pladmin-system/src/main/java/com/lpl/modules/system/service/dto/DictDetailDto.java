package com.lpl.modules.system.service.dto;

import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lpl
 * 数据字典详情数据传输对象
 */
@Getter
@Setter
public class DictDetailDto extends BaseDTO {

    private Long id;

    private DictSmallDto dict;

    private String label;

    private String value;

    private Integer dictSort;
}
