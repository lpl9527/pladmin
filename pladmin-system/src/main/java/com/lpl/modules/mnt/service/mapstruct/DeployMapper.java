package com.lpl.modules.mnt.service.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.mnt.domain.Deploy;
import com.lpl.modules.mnt.service.dto.DeployDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 部署mapstruct映射接口
 */
@Mapper(componentModel = "spring",uses = {AppMapper.class, ServerDeployMapper.class},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeployMapper extends BaseMapper<DeployDto, Deploy> {

}
