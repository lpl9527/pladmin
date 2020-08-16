package com.lpl.modules.system.service.impl;

import com.lpl.config.FileProperties;
import com.lpl.exception.EntityExistException;
import com.lpl.exception.EntityNotFoundException;
import com.lpl.modules.security.service.OnlineUserService;
import com.lpl.modules.security.service.UserCacheClean;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.mapstruct.UserMapper;
import com.lpl.modules.system.repository.UserRepository;
import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.dto.JobSmallDto;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.modules.system.service.dto.UserQueryCriteria;
import com.lpl.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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

    private final OnlineUserService onlineUserService;
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
     * 根据用户id查询
     * @param id
     */
    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public UserDto findById(Long id) {
        User user = userRepository.findById(id).orElseGet(User::new);
        ValidationUtil.isNull(user.getId(), "User", "id", id);
        return userMapper.toDto(user);
    }

    /**
     * 分页查询用户
     * @param criteria 查询条件
     * @param pageable 分页参数
     */
    @Override
    public Object queryAll(UserQueryCriteria criteria, Pageable pageable) {
        Page<User> page = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder), pageable);
        return PageUtil.toPage(page.map(userMapper::toDto));
    }

    /**
     * 查询所有，不分页
     * @param criteria 查询条件
     */
    @Override
    public List<UserDto> queryAll(UserQueryCriteria criteria) {
        List<User> users = userRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder));
        return userMapper.toDto(users);
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
     * 新增用户
     * @param user
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(User user) {
        //根据用户名判断用户是否已经存在
        if (null != userRepository.findByUsername(user.getUsername())) {
            throw new EntityExistException(User.class, "username", user.getUsername());
        }
        //根据邮箱判断邮箱是否已经存在
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new EntityExistException(User.class, "email", user.getEmail());
        }
        userRepository.save(user);
    }

    /**
     * 批量删除用户
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
        for (Long id : ids) {
            //清理缓存
            UserDto userDto = findById(id);
            delCaches(userDto.getId(), userDto.getUsername());
        }
        userRepository.deleteAllByIdIn(ids);
    }

    /**
     * 编辑用户
     * @param resources 更新用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(User resources) {
        //先根据id查询用户
        User user = userRepository.findById(resources.getId()).orElseGet(User::new);
        //根据邮箱和用户名查询用户
        User user1 = userRepository.findByUsername(resources.getUsername());
        User user2 = userRepository.findByEmail(resources.getEmail());
        //更新时保证用户名和邮箱不能重复
        if (user1 != null && !user.getId().equals(user1.getId())) {
            throw new EntityExistException(User.class, "username", resources.getUsername());
        }
        if (user2 != null && !user.getId().equals(user2.getId())) {
            throw new EntityExistException(User.class, "email", resources.getEmail());
        }

        // 如果用户的角色改变，清理缓存
        if (!resources.getRoles().equals(user.getRoles())) {
            redisUtils.del(CacheKey.DATA_USER + resources.getId());
            redisUtils.del(CacheKey.MENU_USER + resources.getId());
            redisUtils.del(CacheKey.ROLE_AUTH + resources.getId());
        }
        // 如果用户名称修改
        if(!resources.getUsername().equals(user.getUsername())){
            redisUtils.del("user::username:" + user.getUsername());
        }
        // 如果用户被禁用，则清除用户登录信息
        if(!resources.getEnabled()){
            onlineUserService.kickOutForUsername(resources.getUsername());
        }
        user.setUsername(resources.getUsername());
        user.setEmail(resources.getEmail());
        user.setEnabled(resources.getEnabled());
        user.setRoles(resources.getRoles());
        user.setDept(resources.getDept());
        user.setJobs(resources.getJobs());
        user.setPhone(resources.getPhone());
        user.setNickName(resources.getNickName());
        user.setGender(resources.getGender());
        //保存用户
        userRepository.save(user);
        // 清除缓存
        delCaches(user.getId(), user.getUsername());
    }

    /**
     * 导出用户数据
     * @param userDtos 待导出的数据
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<UserDto> userDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (UserDto userDto : userDtos) {
            //获取用户角色列表
            List<String> roles = userDto.getRoles().stream().map(RoleSmallDto::getName).collect(Collectors.toList());
            //表格每一行数据
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("用户名", userDto.getUsername());
            map.put("角色", roles);
            map.put("部门", userDto.getDept().getName());
            map.put("岗位", userDto.getJobs().stream().map(JobSmallDto::getName).collect(Collectors.toList()));
            map.put("邮箱", userDto.getEmail());
            map.put("状态", userDto.getEnabled() ? "启用" : "禁用");
            map.put("手机号码", userDto.getPhone());
            map.put("修改密码的时间", userDto.getPwdResetTime());
            map.put("创建日期", userDto.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
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
