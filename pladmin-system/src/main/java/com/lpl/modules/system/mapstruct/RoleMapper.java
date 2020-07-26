package com.lpl.modules.system.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.system.domain.Role;
import com.lpl.modules.system.service.dto.RoleDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * 角色MapStruct接口
 */
@Mapper(componentModel = "spring",
        uses = {
            MenuMapper.class, DeptMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RoleMapper extends BaseMapper<RoleDto, Role> {

}
