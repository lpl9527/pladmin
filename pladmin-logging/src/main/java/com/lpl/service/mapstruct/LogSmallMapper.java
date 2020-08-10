package com.lpl.service.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.domain.Log;
import com.lpl.service.dto.LogSmallDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 部分日志Mapstruct映射接口
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LogSmallMapper extends BaseMapper<LogSmallDto, Log> {

}