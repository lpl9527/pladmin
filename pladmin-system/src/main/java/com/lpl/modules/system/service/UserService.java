package com.lpl.modules.system.service;

import com.lpl.modules.system.service.dto.UserDto;

/**
 * @author lpl
 * 用户Service接口
 */
public interface UserService {

    /**
     * 根据用户名查询用户
     */
    UserDto findByName(String username);
}
