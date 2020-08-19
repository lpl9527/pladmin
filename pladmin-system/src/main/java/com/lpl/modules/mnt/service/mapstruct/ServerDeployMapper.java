package com.lpl.modules.mnt.service.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.mnt.domain.ServerDeploy;
import com.lpl.modules.mnt.service.dto.ServerDeployDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 服务器部署mapstruct映射接口
 */
@Mapper(componentModel = "spring",uses = {},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ServerDeployMapper extends BaseMapper<ServerDeployDto, ServerDeploy> {
}
