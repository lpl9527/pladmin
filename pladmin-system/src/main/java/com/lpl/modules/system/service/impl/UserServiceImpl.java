package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author lpl
 * 用户Service实现类
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * 根据用户名查询用户
     */
    @Override
    public UserDto findByName(String username) {
        return null;
    }
}
