package com.lpl.modules.mnt.service.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.mnt.domain.App;
import com.lpl.modules.mnt.service.dto.AppDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 应用mapstruct映射接口
 */
@Mapper(componentModel = "spring",uses = {},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AppMapper extends BaseMapper<AppDto, App> {

}
