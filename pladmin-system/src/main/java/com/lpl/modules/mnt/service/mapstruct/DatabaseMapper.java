package com.lpl.modules.mnt.service.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.mnt.domain.Database;
import com.lpl.modules.mnt.service.dto.DatabaseDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 数据库mapstruct映射接口
 */
@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DatabaseMapper extends BaseMapper<DatabaseDto, Database> {

}
