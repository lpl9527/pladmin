package com.lpl.modules.system.service.impl;

import ch.qos.logback.core.util.FileUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.lpl.exception.BadRequestException;
import com.lpl.exception.EntityExistException;
import com.lpl.modules.system.domain.Menu;
import com.lpl.modules.system.domain.Role;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.domain.vo.MenuMetaVo;
import com.lpl.modules.system.domain.vo.MenuVo;
import com.lpl.modules.system.mapstruct.MenuMapper;
import com.lpl.modules.system.repository.MenuRepository;
import com.lpl.modules.system.repository.UserRepository;
import com.lpl.modules.system.service.MenuService;
import com.lpl.modules.system.service.RoleService;
import com.lpl.modules.system.service.dto.MenuDto;
import com.lpl.modules.system.service.dto.MenuQueryCriteria;
import com.lpl.modules.system.service.dto.RoleSmallDto;
import com.lpl.utils.*;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
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
    private final RedisUtils redisUtils;
    private final UserRepository userRepository;

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
     * 根据id查询
     * @param id
     */
    @Override
    public Menu findOne(Long id) {
        Menu menu = menuRepository.findById(id).orElseGet(Menu::new);
        ValidationUtil.isNull(menu.getId(),"Menu","id",id);
        return menu;
    }

    /**
     * 获取待删除的菜单
     * @param menuList 待删除的本级菜单列表
     * @param menuSet 待删除的菜单集合
     */
    @Override
    public Set<Menu> getDeleteMenus(List<Menu> menuList, Set<Menu> menuSet) {
        // 递归找出待删除的菜单
        for (Menu menu : menuList) {
            menuSet.add(menu);
            List<Menu> menus = menuRepository.findByPid(menu.getId());
            if(menus!=null && menus.size()!=0){
                getDeleteMenus(menus, menuSet);
            }
        }
        return menuSet;
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

    /**
     * 根据父id加载菜单数据
     * @param pid
     */
    @Override
    @Cacheable(key = "'pid:' + #p0")
    public List<MenuDto> getMenus(Long pid) {
        List<Menu> menus;
        //如果不是根菜单
        if (null != pid && !pid.equals(0L)) {
            menus = menuRepository.findByPid(pid);
        }else {
            menus = menuRepository.findByPidIsNull();
        }
        return menuMapper.toDto(menus);
    }

    /**
     * 根据id查询菜单
     * @param id
     */
    @Override
    @Cacheable(key = "'id:' + #p0")
    public MenuDto findById(Long id) {
        Menu menu = menuRepository.findById(id).orElseGet(Menu::new);
        ValidationUtil.isNull(menu.getId(),"Menu","id",id);
        return menuMapper.toDto(menu);
    }

    /**
     * 根据条件查询全部菜单数据
     * @param criteria
     * @param isQuery
     * @throws Exception
     */
    @Override
    public List<MenuDto> queryAll(MenuQueryCriteria criteria, Boolean isQuery) throws Exception {
        //设置排序
        Sort sort = new Sort(Sort.Direction.ASC, "menuSort");
        if (isQuery) {      //查询全部
            criteria.setPidIsNull(true);
            List<Field> fields = QueryHelp.getAllFields(criteria.getClass(), new ArrayList<>());
            //设置所有查询条件
            for (Field field : fields) {
                //设置对象的访问权限，保证对private的属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if("pidIsNull".equals(field.getName())){
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        //查询
        return menuMapper.toDto(menuRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),sort));
    }

    /**
     * 查询同级及上级菜单
     * @param menuDto  上级菜单对象
     * @param menus 菜单集合
     */
    @Override
    public List<MenuDto> getSuperior(MenuDto menuDto, List<Menu> menus) {
        //如果已经是顶级部门
        if(menuDto.getPid() == null){
            menus.addAll(menuRepository.findByPidIsNull());
            return menuMapper.toDto(menus);
        }
        menus.addAll(menuRepository.findByPid(menuDto.getPid()));
        //递归调用查询
        return getSuperior(findById(menuDto.getPid()), menus);
    }

    /**
     * 新增菜单
     * @param menu
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Menu menu) {
        //保证菜单标题不能重复
        if (menuRepository.findByTitle(menu.getTitle()) != null) {
            throw new EntityExistException(Menu.class,"title",menu.getTitle());
        }
        //保证组件名称不能重复
        if (StringUtils.isNotBlank(menu.getComponentName())) {
            if (menuRepository.findByComponentName(menu.getComponentName()) != null) {
                throw new EntityExistException(Menu.class,"componentName",menu.getComponentName());
            }
        }
        if(menu.getPid().equals(0L)){
            menu.setPid(null);
        }
        //外链菜单检查
        if (menu.getIFrame()) {
            String http = "http://", https = "https://";
            if (!(menu.getPath().toLowerCase().startsWith(http)||menu.getPath().toLowerCase().startsWith(https))) {
                throw new BadRequestException("外链必须以http://或者https://开头");
            }
        }
        //设置子节点数目为 0
        menu.setSubCount(0);
        //保存菜单
        menuRepository.save(menu);
        //更新父节点菜单数目
        updateSubCnt(menu.getPid());

        //清理缓存
        redisUtils.del("menu::pid:" + (menu.getPid() == null ? 0 : menu.getPid()));
    }

    /**
     * 编辑菜单
     * @param resources
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Menu resources) {
       if (resources.getId().equals(resources.getPid())) {
           throw new BadRequestException("上级菜单不能为自己！");
       }
       //根据id查出菜单，对一些参数进行校验
        Menu menu = menuRepository.findById(resources.getId()).orElseGet(Menu::new);
        ValidationUtil.isNull(menu.getId(),"Permission","id",resources.getId());
        if(resources.getIFrame()){
            String http = "http://", https = "https://";
            if (!(resources.getPath().toLowerCase().startsWith(http)||resources.getPath().toLowerCase().startsWith(https))) {
                throw new BadRequestException("外链必须以http://或者https://开头");
            }
        }
        //标题不能重复
        Menu menu1 = menuRepository.findByTitle(resources.getTitle());
        if(menu1 != null && !menu1.getId().equals(menu.getId())){
            throw new EntityExistException(Menu.class,"title",resources.getTitle());
        }

        //记录更新前菜单的父id
        Long oldPid = menu.getPid();
        //记录要更新的菜单的父id
        Long newPid = resources.getPid();

        //组件名称不能重复
        if(StringUtils.isNotBlank(resources.getComponentName())){
            menu1 = menuRepository.findByComponentName(resources.getComponentName());
            if(menu1 != null && !menu1.getId().equals(menu.getId())){
                throw new EntityExistException(Menu.class,"componentName",resources.getComponentName());
            }
        }
        if(resources.getPid().equals(0L)){
            resources.setPid(null);
        }

        menu.setTitle(resources.getTitle());
        menu.setComponent(resources.getComponent());
        menu.setPath(resources.getPath());
        menu.setIcon(resources.getIcon());
        menu.setIFrame(resources.getIFrame());
        menu.setPid(resources.getPid());
        menu.setMenuSort(resources.getMenuSort());
        menu.setCache(resources.getCache());
        menu.setHidden(resources.getHidden());
        menu.setComponentName(resources.getComponentName());
        menu.setPermission(resources.getPermission());
        menu.setType(resources.getType());
        //保存菜单

        //更新父节点下子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);

        //清理缓存
        delCaches(resources.getId(), oldPid, newPid);
    }

    /**
     * 批量删除菜单
     * @param menuSet
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<Menu> menuSet) {
        for (Menu menu : menuSet) {
            // 清理缓存
            delCaches(menu.getId(), menu.getPid(), null);
            //角色解绑菜单，删除角色菜单关系
            roleService.untiedMenu(menu.getId());
            //根据id删除菜单
            menuRepository.deleteById(menu.getId());
            //更新菜单父节点菜单数目
            updateSubCnt(menu.getPid());
        }
    }

    /**
     * 导出菜单数据
     * @param menuDtos 待导出的数据
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<MenuDto> menuDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (MenuDto menuDTO : menuDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("菜单标题", menuDTO.getTitle());
            map.put("菜单类型", menuDTO.getType() == null ? "目录" : menuDTO.getType() == 1 ? "菜单" : "按钮");
            map.put("权限标识", menuDTO.getPermission());
            map.put("外链菜单", menuDTO.getIFrame() ? "是" : "否");
            map.put("菜单可见", menuDTO.getHidden() ? "否" : "是");
            map.put("是否缓存", menuDTO.getCache() ? "是" : "否");
            map.put("创建日期", menuDTO.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 更新菜单父节点菜单数目
     * @param menuId
     */
    private void updateSubCnt(Long menuId){
        if(menuId != null){
            //获取菜单下节点数量
            int count = menuRepository.countByPid(menuId);
            //更新节点数量
            menuRepository.updateSubCntById(count, menuId);
        }
    }

    /**
     * 清理缓存
     * @param id 菜单id
     * @param oldPid 旧的菜单父级id
     * @param newPid 新的菜单父级id
     */
    public void delCaches(Long id, Long oldPid, Long newPid){
        //根据菜单id查询用户列表
        List<User> users = userRepository.findByMenuId(id);
        //删除相关缓存
        redisUtils.del("menu::id:" +id);
        redisUtils.delByKeys("menu::user:",users.stream().map(User::getId).collect(Collectors.toSet()));
        redisUtils.del("menu::pid:" + (oldPid == null ? 0 : oldPid));
        redisUtils.del("menu::pid:" + (newPid == null ? 0 : newPid));

        //根据菜单id查询角色列表
        List<Role> roles = roleService.findInMenuId(new ArrayList<Long>(){{
            add(id);
            add(newPid == null ? 0 : newPid);
        }});
        // 清除 Role 缓存
        redisUtils.delByKeys("role::id:",roles.stream().map(Role::getId).collect(Collectors.toSet()));
    }
}
