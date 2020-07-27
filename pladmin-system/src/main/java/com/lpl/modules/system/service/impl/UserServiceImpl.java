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
import org.springframework.transaction.annotation.Transactional;

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
    @Transactional      //由于Session是被立即关闭的，在我们读取了类的基本属性后，Session已经关闭了，再进行懒加载就会异常。解决：在方法上加上事务。
    public UserDto findByName(String username) {
        User user = userRepository.findByUsername(username);
        if (null == user) {
            throw new EntityNotFoundException(User.class, "name", username);
        }else {
            return userMapper.toDto(user);  //将Entity转为MapStruct映射的Dto
        }
    }
}
