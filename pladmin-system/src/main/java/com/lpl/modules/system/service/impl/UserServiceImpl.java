package com.lpl.modules.system.service.impl;

import com.lpl.exception.EntityNotFoundException;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.mapstruct.UserMapper;
import com.lpl.modules.system.repository.UserRepository;
import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * @author lpl
 * 用户Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    /**
     * 根据用户名查询用户
     */
    @Override
    @Cacheable(key = "'username:' + #p0")
    public UserDto findByName(String username) {
        User user = userRepository.findByUsername(username);
        if (null == user) {
            throw new EntityNotFoundException(User.class, "name", username);
        }else {
            return userMapper.toDto(user);  //将Entity转为MapStruct映射的Dto
        }
    }
}
