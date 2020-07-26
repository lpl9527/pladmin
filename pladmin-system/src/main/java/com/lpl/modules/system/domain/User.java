package com.lpl.modules.system.domain;

import com.lpl.base.BaseEntity;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.Objects;
import java.util.Set;

/**
 * @author lpl
 * 用户数据访问对象
 */
@Getter
@Setter
@Entity
@Table(name = "sys_user")   //指定实体对应数据库表名
public class User extends BaseEntity {

    @Id     //主键
    @Column(name = "user_id")   //字段
    @NotNull(groups = Update.class)     //非空
    @GeneratedValue(strategy = GenerationType.IDENTITY)  //指定按照数据库自动生成策略来生成值
    @ApiModelProperty(value = "用户id", hidden = true)
    private Long id;

    @ManyToMany     //与角色多对多关系
    @ApiModelProperty(value = "用户角色")
    @JoinTable(name = "sys_users_roles",    //指定关联表
                joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
                inverseJoinColumns = {@JoinColumn(name = "role_id", referencedColumnName = "role_id")})
    private Set<Role> roles;

    @ManyToMany
    @ApiModelProperty(value = "用户岗位")
    @JoinTable(name = "sys_users_jobs",
                joinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "user_id")},
                inverseJoinColumns = {@JoinColumn(name = "job_id", referencedColumnName = "job_id")})
    private Set<Job> jobs;

    @OneToOne
    @JoinColumn(name = "dept_id")   //从外部加入一个字段
    @ApiModelProperty(value = "用户部门")
    private Dept dept;

    @NotBlank
    @Column(unique = true)
    @ApiModelProperty(value = "用户名称")
    private String username;

    @NotBlank
    @ApiModelProperty(value = "用户昵称")
    private String nickName;

    @Email
    @NotBlank
    @ApiModelProperty(value = "用户邮箱")
    private String email;

    @NotBlank
    @ApiModelProperty(value = "用户手机号码")
    private String phone;

    @ApiModelProperty(value = "用户性别")
    private String gender;

    @ApiModelProperty(value = "用户头像名称", hidden = true)
    private String avatarName;

    @ApiModelProperty(value = "用户头像存储路径", hidden = true)
    private String avatarPath;

    @ApiModelProperty(value = "用户密码")
    private String password;

    @NotNull
    @ApiModelProperty(value = "是否启用")
    private Boolean enabled;

    @ApiModelProperty(value = "用户是否为Admin账号", hidden = true)
    private Boolean isAdmin = false;

    @Column(name = "pwd_reset_time")
    @ApiModelProperty(value = "最后修改密码事时间", hidden = true)
    private Date pwdResetTime;

    /**
     * 重写equals()方法
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {    //内存地址相同，肯定为同一对象，返回true
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) { //Class对象不同，可定不相等（无论是值还是地址）
            return false;
        }
        User user = (User) o;
        return Objects.equals(id, user.id);     //id相同时才认为两用户对象相等
    }
    /**
     * 重写hashCode()方法
     */
    @Override
    public int hashCode(){
        return Objects.hash(this.id);
    }
}
