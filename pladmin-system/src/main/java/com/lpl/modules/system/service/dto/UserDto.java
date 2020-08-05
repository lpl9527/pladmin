package com.lpl.modules.system.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lpl.base.BaseDTO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

/**
 * @author lpl
 * 用户数据传输对象
 */
@Getter
@Setter
public class UserDto extends BaseDTO {

    /**
     *  id
     */
    private Long id;
    /**
     * 用户名
     */
    private String username;
    /**
     * 昵称
     */
    private String nickName;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 电话
     */
    private String phone;
    /**
     * 性别
     */
    private String gender;
    /**
     * 头像名称
     */
    private String avatarName;
    /**
     * 头像路径
     */
    private String avatarPath;
    /**
     * 是否可用，0不可用，1可用
     */
    private Boolean enabled;
    /**
     * 密码重置时间
     */
    private Date pwdResetTime;
    /**
     * 部门id
     */
    private Long deptId;
    /**
     * 密码
     */
    @JsonIgnore
    private String password;
    /**
     * 是否是管理员
     */
    @JsonIgnore
    private Boolean isAdmin = false;


    /**
     * 用户拥有的角色集合
     */
    private Set<RoleSmallDto> roles;
    /**
     * 用户拥有的职位集合
     */
    private Set<JobSmallDto> jobs;
    /**
     * 用户所在部门
     */
    private DeptSmallDto dept;

}
