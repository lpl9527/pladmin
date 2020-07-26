package com.lpl.modules.system.service;

import com.lpl.modules.system.service.dto.UserDto;

import java.util.List;

/**
 * @author lpl
 * 数据权限Service接口
 */
public interface DataService {

    /**
     * 根据用户对象获取数据权限部门id列表
     * @param userDto
     */
    List<Long> getDeptIds(UserDto userDto);
}
