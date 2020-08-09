package com.lpl.modules.system.service.impl;

import com.lpl.config.FileProperties;
import com.lpl.exception.EntityNotFoundException;
import com.lpl.modules.security.service.UserCacheClean;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.mapstruct.UserMapper;
import com.lpl.modules.system.repository.UserRepository;
import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private final FileProperties fileProperties;    //文件配置类

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
     * 根据用户名更新用户邮箱
     * @param username  用户名
     * @param email 新邮箱
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateEmail(String username, String email) {
        //更新邮箱
        userRepository.updateEmail(username, email);

        //清除redis及系统的用户缓存
        redisUtils.del(CacheKey.USER_NAME + username);
        flushCache(username);
    }

    /**
     * 修改用户头像
     * @param multipartFile  用户头像图片文件
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, String> updateAvatar(MultipartFile multipartFile) {

        //根据用户名查询用户对象
        User user = userRepository.findByUsername(SecurityUtils.getCurrentUsername());
        //获取原来的头像路径
        String oldPath = user.getAvatarPath();

        //文件上传
        File file = FileUtils.upload(multipartFile, fileProperties.getPath().getAvatar());
        //更新文件信息
        user.setAvatarName(file.getName());
        user.setAvatarPath(Objects.requireNonNull(file).getPath());
        //保存用户
        userRepository.save(user);
        if (StringUtils.isNotBlank(oldPath)) {
            //删除原来的头像文件
            FileUtils.del(oldPath);
        }
        //根据用户名删除缓存信息
        @NotBlank String username = user.getUsername();
        redisUtils.del(CacheKey.USER_NAME + username);
        flushCache(username);

        return new HashMap<String, String>(1){{
            put("avatar", file.getName());
        }};
    }

    /**
     * 个人中心修改用户资料
     * @param user
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateCenter(User user) {

        //根据用户id查询用户
        User newUser = userRepository.findById(user.getId()).orElseGet(User::new);
        //更新用户信息
        newUser.setNickName(user.getNickName());
        newUser.setPhone(user.getPhone());
        newUser.setGender(user.getGender());
        //保存用户
        userRepository.save(newUser);
        //清理redis和系统用户信息缓存
        delCaches(newUser.getId(), newUser.getUsername());
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
