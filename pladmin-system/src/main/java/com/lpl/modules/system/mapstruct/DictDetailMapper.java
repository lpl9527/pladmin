package com.lpl.modules.system.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.system.domain.DictDetail;
import com.lpl.modules.system.service.dto.DictDetailDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 数据字典详情Mapstruct接口
 */
@Mapper(componentModel = "spring", uses = {DictSmallMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DictDetailMapper extends BaseMapper<DictDetailDto, DictDetail> {

}
