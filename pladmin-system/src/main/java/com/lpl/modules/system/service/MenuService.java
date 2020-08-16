package com.lpl.modules.system.service;

import com.lpl.modules.system.domain.Menu;
import com.lpl.modules.system.domain.vo.MenuVo;
import com.lpl.modules.system.service.dto.MenuDto;
import com.lpl.modules.system.service.dto.MenuQueryCriteria;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 菜单Service接口
 */
public interface MenuService {

    /**
     * 获取当前用户的菜单列表
     * @param userId    当前用户id
     */
    List<MenuDto> findByUser(Long userId);

    /**
     * 根据id查询
     * @param id
     */
    Menu findOne(Long id);

    /**
     * 获取待删除的菜单
     * @param menuList 待删除的本级菜单列表
     * @param menuSet 待删除的菜单集合
     */
    Set<Menu> getDeleteMenus(List<Menu> menuList, Set<Menu> menuSet);

    /**
     * 构建菜单树
     * @param menuDtos  菜单列表
     */
    List<MenuDto> buildTree(List<MenuDto> menuDtos);

    /**
     * 构建菜单树
     * @param menuDtos
     */
    List<MenuVo> buildMenus(List<MenuDto> menuDtos);

    /**
     * 根据父id加载菜单数据
     * @param pid
     */
    List<MenuDto> getMenus(Long pid);

    /**
     * 根据id查询菜单
     * @param id
     */
    MenuDto findById(Long id);

    /**
     * 根据条件查询全部菜单数据
     * @param criteria
     * @param isQuery
     * @throws Exception
     */
    List<MenuDto> queryAll(MenuQueryCriteria criteria, Boolean isQuery) throws Exception;

    /**
     * 查询同级及上级菜单
     * @param menuDto  上级菜单对象
     * @param menus 菜单集合
     */
    List<MenuDto> getSuperior(MenuDto menuDto, List<Menu> menus);

    /**
     * 新增菜单
     * @param menu
     */
    void create(Menu menu);

    /**
     * 编辑菜单
     * @param menu
     */
    void update(Menu menu);

    /**
     * 批量删除菜单
     * @param menuSet
     */
    void delete(Set<Menu> menuSet);

    /**
     * 导出菜单数据
     * @param menuDtos 待导出的数据
     * @param response
     * @throws IOException
     */
    void download(List<MenuDto> menuDtos, HttpServletResponse response) throws IOException;

}
