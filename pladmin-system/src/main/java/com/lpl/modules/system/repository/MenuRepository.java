package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 菜单持久化接口
 */
public interface MenuRepository extends JpaRepository<Menu, Long>, JpaSpecificationExecutor<Menu> {

    /**
     * 根据角色id集合和菜单类型查询菜单集合
     * @param roleIds   角色id集合
     * @param type  不是此类型的菜单
     */
    @Query(value = "select m.* from sys_menu m, sys_roles_menus r where m.menu_id = r.menu_id and r.role_id in ?1 and type != ?2 order by m.menu_sort asc ",
            nativeQuery = true)
    LinkedHashSet<Menu> findByRoleIdsAndTypeNot(Set<Long> roleIds, int type);

    /**
     * 根据pid查询
     * @param pid
     */
    List<Menu> findByPid(long pid);

    /**
     * 查询顶级菜单下菜单列表
     */
    List<Menu> findByPidIsNull();

    /**
     * 根据标题查询菜单
     * @param title
     */
    Menu findByTitle(String title);

    /**
     * 根据组件名称查询用户
     * @param name
     */
    Menu findByComponentName(String name);

    /**
     * 获取菜单下节点数量
     * @param pid
     */
    int countByPid(Long pid);

    /**
     * 更新菜单下的子节点数量
     * @param count 数量
     * @param menuId 节点id
     */
    @Modifying
    @Query(value = "update sys_menu set sub_count = ?1 where menu_id = ?2",
            nativeQuery = true)
    void updateSubCntById(int count, Long menuId);
}
