package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.modules.system.service.dto.UserQueryCriteria;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 根据用户id查询
     * @param id
     */
    UserDto findById(Long id);

    /**
     * 分页查询用户
     * @param criteria 查询条件
     * @param pageable 分页参数
     */
    Object queryAll(UserQueryCriteria criteria, Pageable pageable);

    /**
     * 查询所有，不分页
     * @param criteria 查询条件
     */
    List<UserDto> queryAll(UserQueryCriteria criteria);

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

    /**
     * 新增用户
     * @param user
     */
    void create(User user);

    /**
     * 批量删除用户
     * @param ids
     */
    void delete(Set<Long> ids);

    /**
     * 编辑用户
     * @param user
     */
    void update(User user);

    /**
     * 导出用户数据
     * @param userDtos 待导出的数据
     * @param response
     * @throws IOException
     */
    void download(List<UserDto> userDtos, HttpServletResponse response) throws IOException;
}
