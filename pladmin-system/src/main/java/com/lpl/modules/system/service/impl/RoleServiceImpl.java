package com.lpl.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.lpl.exception.BadRequestException;
import com.lpl.exception.EntityExistException;
import com.lpl.modules.security.service.UserCacheClean;
import com.lpl.modules.system.domain.Menu;
import com.lpl.modules.system.domain.Role;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.mapstruct.RoleMapper;
import com.lpl.modules.system.mapstruct.RoleSmallMapper;
import com.lpl.modules.system.repository.RoleRepository;
import com.lpl.modules.system.repository.UserRepository;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.dto.RoleDto;
import com.lpl.modules.system.service.dto.RoleQueryCriteria;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.modules.system.service.dto.UserDto;
import com.lpl.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lpl
 * 角色Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "role")
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;    //角色持久层接口
    private final RoleSmallMapper roleSmallMapper;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;
    private final UserCacheClean userCacheClean;
    private final RedisUtils redisUtils;

    /**
     * 根据用户id查询用户的角色对象列表
     * @param userId
     */
    @Override
    public List<RoleSmallDto> findByUserId(Long userId) {
        //获取用户的角色集合
        Set<Role> roleSet = roleRepository.findByUserId(userId);
        //将其转为列表
        List<Role> roles = new ArrayList<>(roleSet);
        //最后将其转换为RoleSmallDto列表
        return roleSmallMapper.toDto(roles);
    }

    /**
     * 获取用户已授权的权限信息列表
     * @param userDto
     */
    @Override
    @Cacheable(key = "'auth:' + #p0.id")    //指定缓存key为 auth: + 用户id 的形式
    public List<GrantedAuthority> mapToGrantedAuthorities(UserDto userDto) {

        Set<String> permissions = new HashSet<>();
        //如果是管理员
        if (userDto.getIsAdmin()) {
            permissions.add("admin");
            return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        }
        //根据用户id查询角色列表
        Set<Role> roles = roleRepository.findByUserId(userDto.getId());
        permissions = roles.stream().flatMap(role -> role.getMenus().stream()).
                filter(menu -> StringUtils.isNotBlank(menu.getPermission())).
                map(Menu::getPermission).collect(Collectors.toSet());
        return permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    /**
     * 查询全部角色
     */
    @Override
    public List<RoleDto> queryAll() {
        Sort sort = new Sort(Sort.Direction.ASC, "level");
        return roleMapper.toDto(roleRepository.findAll(sort));
    }

    /**
     * 带条件分页查询角色
     * @param criteria
     * @param pageable
     */
    @Override
    public Object queryAll(RoleQueryCriteria criteria, Pageable pageable) {
        Page<Role> page = roleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(roleMapper::toDto));
    }

    /**
     * 带条件，不分页查询全部角色数据
     * @param criteria
     */
    @Override
    public List<RoleDto> queryAll(RoleQueryCriteria criteria) {
        return roleMapper.toDto(roleRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root, criteria, criteriaBuilder)));
    }

    /**
     * 根据id查询角色
     * @param id
     */
    @Override
    @Cacheable(key = "'id:' + #p0")
    @Transactional(rollbackFor = Exception.class)
    public RoleDto findById(Long id) {
        Role role = roleRepository.findById(id).orElseGet(Role::new);
        ValidationUtil.isNull(role.getId(), "Role", "id", id);
        return roleMapper.toDto(role);
    }

    /**
     * 根据角色集合查询用户最高角色级别
     * @param roles
     */
    @Override
    public Integer findByRoles(Set<Role> roles) {
        Set<RoleDto> roleDtos = new HashSet<>();
        for (Role role : roles) {
            //根据id查询角色放入集合
            roleDtos.add(findById(role.getId()));
        }
        return Collections.min(roleDtos.stream().map(RoleDto::getLevel).collect(Collectors.toList()));
    }

    /**
     * 创建角色
     * @param role
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Role role) {
        if (roleRepository.findByName(role.getName()) != null) {
            throw new EntityExistException(Role.class, "username", role.getName());
        }
        //保存角色
        roleRepository.save(role);
    }

    /**
     * 更新角色
     * @param resources
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Role resources) {
       //判断是否已经有此角色
        Role role = roleRepository.findById(resources.getId()).orElseGet(Role::new);
        ValidationUtil.isNull(role.getId(), "Role", "id", resources.getId());

        //角色名称不能重复
        Role role1 = roleRepository.findByName(resources.getName());
        if (role1 != null && !role1.getId().equals(role.getId())) {
            throw new EntityExistException(Role.class, "username", resources.getName());
        }

        role.setName(resources.getName());
        role.setDescription(resources.getDescription());
        role.setDataScope(resources.getDataScope());
        role.setDepts(resources.getDepts());
        role.setLevel(resources.getLevel());

        //保存角色
        roleRepository.save(role);
        //更新缓存
        delCaches(role.getId(), null);
    }

    /**
     * 批量删除角色
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Long> ids) {
       //清理缓存
        for (Long id : ids) {
            delCaches(id, null);
        }
        //批量删除
        roleRepository.deleteAllByIdIn(ids);
    }

    /**
     * 验证角色集合是否有被用户关联的角色
     * @param ids
     */
    @Override
    public void verification(Set<Long> ids) {
        //如果根据角色id集合查询到的用户数量大于0，说明存在关联用户
        if (userRepository.countByRoles(ids) > 0) {
            throw new BadRequestException("所选角色存在用户关联，请解除关联再试！");
        }
    }

    /**
     * 导出角色数据
     * @param roles 待导出的角色数据
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<RoleDto> roles, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (RoleDto role : roles) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("角色名称", role.getName());
            map.put("角色级别", role.getLevel());
            map.put("描述", role.getDescription());
            map.put("创建日期", role.getCreateTime());
            list.add(map);
        }
        //导出
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 更新角色菜单
     * @param resources
     * @param roleDto
     */
    @Override
    public void updateMenu(Role resources, RoleDto roleDto) {
        Role role = roleMapper.toEntity(roleDto);
        List<User> users = userRepository.findByRoleId(role.getId());
        //更新角色菜单
        role.setMenus(resources.getMenus());
        //清理缓存
        delCaches(resources.getId(), users);
        //保存角色
        roleRepository.save(role);
    }

    /**
     * 根据菜单id查询角色列表
     * @param menuIds
     */
    @Override
    public List<Role> findInMenuId(List<Long> menuIds) {
        return roleRepository.findInMenuId(menuIds);
    }

    /**
     * 根据菜单id解绑角色
     * @param menuId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void untiedMenu(Long menuId) {
        //解绑角色菜单
        roleRepository.untiedMenu(menuId);
    }

    /**
     * 清理角色关联的相关缓存信息
     * @param id
     * @param users
     */
    public void delCaches(Long id, List<User> users) {
        //获取当前角色关联的用户集合
        users = CollectionUtil.isEmpty(users) ? userRepository.findByRoleId(id) : users;
        if (CollectionUtil.isNotEmpty(users)) {
            //遍历清理用户缓存
            users.forEach(item -> userCacheClean.cleanUserCache(item.getUsername()));
            //获取用户id列表
            Set<Long> userIds = users.stream().map(User::getId).collect(Collectors.toSet());

            //清理相关缓存
            redisUtils.delByKeys(CacheKey.DATA_USER, userIds);
            redisUtils.delByKeys(CacheKey.MENU_USER, userIds);
            redisUtils.delByKeys(CacheKey.ROLE_AUTH, userIds);
            redisUtils.del(CacheKey.ROLE_ID + id);
        }
    }

}
