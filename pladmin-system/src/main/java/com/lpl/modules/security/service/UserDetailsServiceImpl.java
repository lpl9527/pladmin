package com.lpl.modules.security.service;

import com.lpl.exception.EntityNotFoundException;
import com.lpl.modules.system.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * @author lpl
 * 实现UserDetailsService接口进行用户名密码校验
 */
@RequiredArgsConstructor
@Service("userDetailsService")
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * 根据用户名获取用户信息
     * @param username  待认证的用户名
     * @return  UserDetails对象，存放用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        UserDto userDto;
        try{

        }catch (EntityNotFoundException e) {
            // SpringSecurity会自动转换UsernameNotFoundException为BadCredentialsException
            throw new UsernameNotFoundException("", e);
        }
        return null;
    }
}
