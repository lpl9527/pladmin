package com.lpl.modules.security.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lpl.modules.system.service.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author lpl
 * 实现了UserDetails接口，存放从数据库查出的用户认证信息，用于认证比对
 */
@Getter
@AllArgsConstructor
public class JwtUserDto implements UserDetails {

    private final UserDto user;  //用户数据传输对象

    private final List<Long> dataScopes; //数据权限列表

    @JsonIgnore
    private final List<GrantedAuthority> authorities;   //已经授权的认证列表

    /**
     * 获取拥有的角色
     */
    public Set<String> getRoles() {
        return authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    }

    /**
     * 获取密码
     */
    @Override
    @JsonIgnore
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * 获取用户名
     */
    @Override
    @JsonIgnore
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * 账户有效（未过期）
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * 账户未被锁
     */
    @Override
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * 认证未过期
     */
    @Override
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * 是否可用
     */
    @Override
    @JsonIgnore
    public boolean isEnabled() {
        return user.getEnabled();
    }
}
