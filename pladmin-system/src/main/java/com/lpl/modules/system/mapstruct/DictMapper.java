package com.lpl.modules.system.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.system.domain.Dict;
import com.lpl.modules.system.service.dto.DictDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 字典Mapstruct接口
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictMapper extends BaseMapper<DictDto, Dict> {

}
