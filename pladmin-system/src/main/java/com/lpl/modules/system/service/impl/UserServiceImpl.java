package com.lpl.modules.system.service.impl;

import com.lpl.exception.EntityNotFoundException;
import com.lpl.modules.security.service.UserCacheClean;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.mapstruct.UserMapper;
import com.lpl.modules.system.repository.UserRepository;
import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.utils.CacheKey;
import com.lpl.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author lpl
 * 用户Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "user")
public class UserServiceImpl implements UserService {

    private final RedisUtils redisUtils;
    private final UserCacheClean userCacheClean;    //用户登录信息缓存清理工具类

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

    /**
     * 修改密码
     * @param username  用户名
     * @param encryptPassword   Rsa加密后的密码
     */
    @Override
    @Transactional(rollbackFor = Exception.class)    //发生异常时回滚
    public void updatePass(String username, String encryptPassword) {
        //修改数据库密码
        userRepository.updatePass(username, encryptPassword, new Date());
        //删除redis缓存的用户信息
        redisUtils.del(CacheKey.USER_NAME + username);
        //删除ConcurrentHashMap中的用户缓存
        flushCache(username);
    }

    /**
     * 清理当前用户缓存信息
     * @param username
     */
    private void flushCache(String username) {
        userCacheClean.cleanUserCache(username);
    }

    /**
     * 清理所有用户缓存信息（包括redis中缓存）
     * @param id    用户id
     * @param username  用户名
     */
    public void delCaches(Long id, String username) {
        redisUtils.del(CacheKey.USER_ID + id);
        redisUtils.del(CacheKey.USER_NAME + username);
        flushCache(username);
    }
}
