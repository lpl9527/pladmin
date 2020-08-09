package com.lpl.service.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.domain.Log;
import com.lpl.service.dto.LogErrorDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * Error类型日志Mapstruct映射接口
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LogErrorMapper extends BaseMapper<LogErrorDto, Log> {

}
