package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.modules.system.service.dto.UserQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * @author lpl
 * 用户Service接口
 */
public interface UserService {

    /**
     * 根据用户名查询用户
     */
    UserDto findByName(String username);

    /**
     * 分页查询用户
     * @param criteria 查询条件
     * @param pageable 分页参数
     */
    Object queryAll(UserQueryCriteria criteria, Pageable pageable);

    /**
     * 修改密码
     * @param username  用户名
     * @param encryptPassword   Rsa加密后的密码
     */
    void updatePass(String username, String encryptPassword);

    /**
     * 根据用户名更新用户邮箱
     * @param username  用户名
     * @param email 新邮箱
     */
    void updateEmail(String username, String email);

    /**
     * 修改用户头像
     * @param file  用户头像图片文件
     */
    Map<String, String> updateAvatar(MultipartFile file);

    /**
     * 个人中心修改用户资料
     * @param user
     */
    void updateCenter(User user);
}
