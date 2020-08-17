package com.lpl.modules.system.service.dto;

import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author lpl
 * 字典数据传输对象
 */
@Getter
@Setter
public class DictDto extends BaseDTO {

    private Long id;

    private List<DictDetailDto> dictDetails;

    private String name;

    private String description;

}
