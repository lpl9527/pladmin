package com.lpl.modules.system.mapstruct;

import com.lpl.base.BaseMapper;
import com.lpl.modules.system.domain.Job;
import com.lpl.modules.system.service.dto.JobDto;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author lpl
 * 岗位MapStruct接口
 */
@Mapper(componentModel = "spring",
        uses = {
            DeptMapper.class
        },
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface JobMapper extends BaseMapper<JobDto, Job> {

}
