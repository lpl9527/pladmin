package com.lpl.modules.security.service;

import com.lpl.exception.BadRequestException;
import com.lpl.exception.EntityNotFoundException;
import com.lpl.modules.security.config.bean.LoginProperties;
import com.lpl.modules.security.service.dto.JwtUserDto;
import com.lpl.modules.system.service.DataService;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private final LoginProperties loginProperties;  //登录工具类

    public void setEnableCache(boolean enableCache) {   //设置是否缓存用户信息
        this.loginProperties.setCacheEnable(enableCache);
    }

    static Map<String, JwtUserDto> userDtoCache = new ConcurrentHashMap<>();    //用于存放从数据库中查询出的用户信息

    /**
     * 根据用户名获取用户信息。此处添加缓存，对用户认证过程中的必要查询进行优化
     * @param username  待认证的用户名
     * @return  UserDetails对象，存放用户信息
     */
    @Override
    public JwtUserDto loadUserByUsername(String username) {

        boolean searchDb = true;   //是否查询数据库
        JwtUserDto jwtUserDto = null;
        //用户开启缓存并且缓存中有此用户，直接从缓存中获取
        if (loginProperties.isCacheEnable() && userDtoCache.containsKey(username)) {
            jwtUserDto = userDtoCache.get(username);
            searchDb = false;   //不查询数据库
        }
        if (searchDb) {     //没有缓存，需要查询数据库
            UserDto user;
            try{
                //根据用户名查询用户信息
                user = userService.findByName(username);
            }catch (EntityNotFoundException e) {
                // SpringSecurity会自动转换UsernameNotFoundException为BadCredentialsException
                throw new UsernameNotFoundException("查询用户信息异常！", e);
            }
            if (null == user) {
                throw new UsernameNotFoundException("用户名或密码错误！");
            }else {
                if (!user.getEnabled()) {
                    throw new BadRequestException("账号未激活！");
                }
                //构建jwtUserDto对象
                jwtUserDto = new JwtUserDto(
                        user,
                        dataService.getDeptIds(user),
                        roleService.mapToGrantedAuthorities(user)
                );
                //将此用户放入缓存
                userDtoCache.put(username, jwtUserDto);
            }
        }

        return jwtUserDto;
    }
}
