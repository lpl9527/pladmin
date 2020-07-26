package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.domain.Dept;
import com.lpl.modules.system.mapstruct.RoleSmallMapper;
import com.lpl.modules.system.service.DataService;
import com.lpl.modules.system.service.DeptService;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.UserService;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.utils.enums.DataScopeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author lpl
 * 数据权限Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "data")
public class DataServiceImpl implements DataService {

    private final RoleService roleService;
    private final DeptService deptService;

    /**
     * 根据用户对象获取数据权限部门id列表
     * @param userDto
     */
    @Override
    @Cacheable(key = "'user:' + #p0.id")    //以user: + 用户id作为主键
    public List<Long> getDeptIds(UserDto userDto) {

        Set<Long> deptIds = new HashSet<>();    //用于存储部门id
        //根据用户id查询用户角色列表
        List<RoleSmallDto> roles = roleService.findByUserId(userDto.getId());

        //根据角色获取部门id列表
        for (RoleSmallDto role : roles) {
            DataScopeEnum dataScopeEnum = DataScopeEnum.find(role.getDataScope());
            switch (Objects.requireNonNull(dataScopeEnum)) {
                case THIS_LEVEL:
                    deptIds.add(userDto.getDept().getId());     //若是本级部门数据权限，则加入到部门id列表
                    break;
                case CUSTOMIZE:
                    deptIds.addAll(getCustomize(deptIds, role));
                    break;
                default:
                    break;
            }
        }
       return new ArrayList<>(deptIds);
    }

    /**
     * 获取自定义的数据权限部门id集合
     * @param deptIds   //部门id列表
     * @param roleSmallDto  //角色
     */
    public Set<Long> getCustomize(Set<Long> deptIds, RoleSmallDto roleSmallDto) {
        //根据角色id查询角色拥有权限的部门
        Set<Dept> depts = deptService.findByRoleId(roleSmallDto.getId());
        if (null != depts || depts.size() > 0) {
            for (Dept dept : depts) {
                deptIds.add(dept.getId());
                //根据部门Id查询其下子部门列表
                List<Dept> deptChildren = deptService.findByPid(dept.getId());
                if (null != deptChildren && deptChildren.size() != 0) {
                    //调用递归方法查询所有层级下的子部门列表
                    List<Long> childrenList = deptService.getDeptChildren(dept.getId(), deptChildren);
                    deptIds.addAll(childrenList);
                }
            }
        }
        return deptIds;
    }
}
