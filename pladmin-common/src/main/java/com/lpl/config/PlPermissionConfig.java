package com.lpl.config;

import com.lpl.utils.SecurityUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author lpl
 * 权限配置类，用于判断用户是否具有访问某接口的权限
 */
@Service(value = "pl")
public class PlPermissionConfig {

    public Boolean check(String ...permissions) {
        //获取当前用户的所有权限列表
        List<String> plPermissions = SecurityUtils.getCurrentUser().getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
        //判断当前用户权限是否包含接口上定义的权限
        return plPermissions.contains("admin") || Arrays.stream(permissions).anyMatch(plPermissions::contains);
    }
}
