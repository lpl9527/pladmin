package com.lpl.modules.system.repository;

import com.lpl.modules.system.domain.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.LinkedHashSet;
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
    LinkedHashSet<Menu> findByRoleIdsAndTypeNot(Set<Long> roleIds, int type);
}
