package com.lpl.modules.system.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.system.domain.Dict;
import com.lpl.modules.system.service.dto.DictSmallDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 数据字典部分属性Mapstruct转换接口
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictSmallMapper extends BaseMapper<DictSmallDto, Dict> {

}
