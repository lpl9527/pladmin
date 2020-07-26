package com.lpl.modules.security.service;

import com.lpl.exception.BadRequestException;
import com.lpl.exception.EntityNotFoundException;
import com.lpl.modules.security.service.dto.JwtUserDto;
import com.lpl.modules.system.service.DataService;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.UserService;
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

    private final UserService userService;
    private final DataService dataService;
    private final RoleService roleService;

    /**
     * 根据用户名获取用户信息
     * @param username  待认证的用户名
     * @return  UserDetails对象，存放用户信息
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        UserDto userDto;
        try{
            //根据用户名查询用户信息
            userDto = userService.findByName(username);
        }catch (EntityNotFoundException e) {
            // SpringSecurity会自动转换UsernameNotFoundException为BadCredentialsException
            throw new UsernameNotFoundException("", e);
        }
        if(null == userDto) {
            throw new UsernameNotFoundException("");
        }else {
            if (!userDto.getEnabled()) {
                throw new BadRequestException("账户未激活！");
            }
            //返回UserDetails对象，携带用户信息，用户具有权限的部门列表，用户已授权的角色权限列表
            return new JwtUserDto(userDto,
                    dataService.getDeptIds(userDto),
                    roleService.mapToGrantedAuthorities(userDto));
        }
    }
}
