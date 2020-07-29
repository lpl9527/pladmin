package com.lpl.modules.system.service.impl;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lpl.modules.system.domain.Menu;
import com.lpl.modules.system.domain.vo.MenuMetaVo;
import com.lpl.modules.system.domain.vo.MenuVo;
import com.lpl.modules.system.mapstruct.MenuMapper;
import com.lpl.modules.system.repository.MenuRepository;
import com.lpl.modules.system.service.MenuService;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.dto.MenuDto;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lpl
 * 菜单Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "menu")
public class MenuServiceImpl implements MenuService {

    private final RoleService roleService;
    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;

    /**
     * 获取当前用户的菜单列表
     * @param userId 当前用户id
     */
    @Override
    @Cacheable(key = "'user:' + #p0")   //指定属于user的缓存
    public List<MenuDto> findByUser(Long userId) {

        //根据当前用户id获取用户的角色列表
        List<RoleSmallDto> roles = roleService.findByUserId(userId);
        //将角色列表提取为角色id集合
        Set<Long> roleIds = roles.stream().map(RoleSmallDto::getId).collect(Collectors.toSet());
        //根据角色id结合查询非按钮类型菜单集合
        LinkedHashSet<Menu> menus = menuRepository.findByRoleIdsAndTypeNot(roleIds, 2);     //类型2表示按钮类型菜单
        //将菜单集合转化为菜单Dto列表
        List<MenuDto> menuDtoList = menus.stream().map(menuMapper::toDto).collect(Collectors.toList());

        return menuDtoList;
    }

    /**
     * 构建菜单树
     * @param menuDtos  菜单列表
     */
    @Override
    public List<MenuDto> buildTree(List<MenuDto> menuDtos) {
        List<MenuDto> tree = new ArrayList<>();
        Set<Long> ids = new HashSet<>();
        for (MenuDto menuDto : menuDtos) {
            //如果是根目录
            if (menuDto.getPid() == null) {
                tree.add(menuDto);
            }
            //不是根目录的收集将每个菜单的子菜单汇总，并收集菜单id集合
            for (MenuDto it : menuDtos) {
                //如果当前菜单下有子菜单
                if (menuDto.getId().equals(it.getPid())) {
                    //没有子菜单时
                    if (menuDto.getChildren() == null) {
                        menuDto.setChildren(new ArrayList<>());
                    }
                    //将此子菜单添加到当前菜单中
                    menuDto.getChildren().add(it);
                    //id放到菜单列表中
                    ids.add(it.getId());
                }
            }
        }
        //如果都没有根目录（都是一级菜单）
        if (tree.size() == 0) {
            tree = menuDtos.stream().filter(s -> !ids.contains(s.getId())).collect(Collectors.toList());
        }
        return tree;
    }

    /**
     * 构建菜单树
     * @param menuDtos
     */
    @Override
    public List<MenuVo> buildMenus(List<MenuDto> menuDtos) {

        //存放结果
        List<MenuVo> list = new LinkedList<>();
        //遍历菜单集合，构建菜单树
        menuDtos.forEach(menuDto -> {
            if (menuDto != null) {
                //获取子菜单列表
                List<MenuDto> menuDtoList = menuDto.getChildren();

                MenuVo menuVo = new MenuVo();
                //设置菜单对应的组件名称
                menuVo.setName(ObjectUtil.isNotEmpty(menuDto.getComponentName()) ? menuDto.getComponentName() : menuDto.getTitle());
                //设置组件路由地址，如果是根路径，就要加上 /
                menuVo.setPath(menuDto.getPid() == null ? "/" + menuDto.getPath() : menuDto.getPath());
                //组件是否隐藏
                menuVo.setHidden(menuDto.getHidden());
                //如果不是外部链接组件
                if (!menuDto.getIFrame()) {
                    //如果是根目录并且组件地址为空，则默认设置为 Layout
                    if (menuDto.getPid() == null) {
                        menuVo.setComponent(StrUtil.isEmpty(menuDto.getComponent()) ? "Layout" : menuDto.getComponent());
                    }else if (!StrUtil.isEmpty(menuDto.getComponent())){
                        menuVo.setComponent(menuDto.getComponent());
                    }
                }
                //设置菜单信息元数据
                menuVo.setMeta(new MenuMetaVo(menuDto.getTitle(), menuDto.getIcon(), !menuDto.getCache()));
                if (menuDtoList != null && menuDtoList.size() != 0){
                    menuVo.setAlwaysShow(true);
                    menuVo.setRedirect("noredirect");
                    //递归构建菜单
                    menuVo.setChildren(buildMenus(menuDtoList));
                }else if (menuDto.getPid() == null) {   //处理一级菜单并且没有子菜单的情况

                    MenuVo menuVo1 = new MenuVo();
                    menuVo1.setMeta(menuVo.getMeta());
                    //非外链
                    if (!menuDto.getIFrame()) {
                        menuVo1.setPath("index");
                        menuVo1.setName(menuVo.getName());
                        menuVo1.setComponent(menuVo.getComponent());
                    }else{
                        menuVo1.setPath(menuDto.getPath());
                    }
                    menuVo.setName(null);
                    menuVo.setMeta(null);
                    menuVo.setComponent("Layout");
                    List<MenuVo> list1 = new ArrayList<>();
                    list1.add(menuVo1);
                    menuVo.setChildren(list1);
                }
                list.add(menuVo);
            }
        });
        return list;
    }
}
