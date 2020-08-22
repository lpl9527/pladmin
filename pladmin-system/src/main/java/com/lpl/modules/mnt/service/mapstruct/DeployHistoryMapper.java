package com.lpl.modules.mnt.service.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.mnt.domain.DeployHistory;
import com.lpl.modules.mnt.service.dto.DeployHistoryDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 部署历史mapstruct映射接口
 */
@Mapper(componentModel = "spring",uses = {},unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeployHistoryMapper extends BaseMapper<DeployHistoryDto, DeployHistory> {

}
