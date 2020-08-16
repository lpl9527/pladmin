package com.lpl.modules.system.rest;

import cn.hutool.core.collection.CollectionUtil;
import com.lpl.annotation.Log;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.domain.Menu;
import com.lpl.modules.system.domain.vo.MenuVo;
import com.lpl.modules.system.mapstruct.MenuMapper;
import com.lpl.modules.system.service.MenuService;
import com.lpl.modules.system.service.dto.MenuDto;
import com.lpl.modules.system.service.dto.MenuQueryCriteria;
import com.lpl.utils.PageUtil;
import com.lpl.utils.SecurityUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * 菜单Controller
 */
@RestController
@RequiredArgsConstructor
@Api(tags = "系统：菜单管理")
@RequestMapping("/api/menus")
public class MenuController {

    private static final String ENTITY_NAME = "menu";
    private final MenuService menuService;
    private final MenuMapper menuMapper;

    /**
     * 根据当前登录用户获取前端所需菜单
     */
    @ApiOperation("获取前端菜单")
    @RequestMapping("/build")
    public ResponseEntity<Object> buildMenus() {
        Long currentUserId = SecurityUtils.getCurrentUserId();
        List<MenuDto> menuDtoList = menuService.findByUser(SecurityUtils.getCurrentUserId());
        //构建菜单树
        List<MenuDto> menuDtos = menuService.buildTree(menuDtoList);
        List<MenuVo> list = menuService.buildMenus(menuDtos);
        return new ResponseEntity<>(list, HttpStatus.OK);
    }

    @Log("查询全部菜单")
    @GetMapping(value = "/lazy")
    @PreAuthorize("@pl.check('menu:list','roles:list')")
    public ResponseEntity<Object> queryAll(@RequestParam Long pid) {
        //根据pid查询
        List<MenuDto> menus = menuService.getMenus(pid);
        return new ResponseEntity<>(menus, HttpStatus.OK);
    }

    @Log("查询菜单")
    @ApiOperation("查询菜单")
    @GetMapping
    @PreAuthorize("@pl.check('menu:list')")
    public ResponseEntity<Object> query(MenuQueryCriteria criteria) throws Exception {
        List<MenuDto> menuDtos = menuService.queryAll(criteria, true);
        return new ResponseEntity<>(PageUtil.toPage(menuDtos, menuDtos.size()), HttpStatus.OK);
    }

    @Log("查询同级及上级菜单")
    @ApiOperation("查询同级及上级菜单")
    @PostMapping(value = "/superior")
    @PreAuthorize("@pl.check('menu:list')")
    public ResponseEntity<Object> getSuperior(@RequestBody List<Long> ids) {
        Set<MenuDto> menuDtos = new LinkedHashSet<>();
        if (CollectionUtil.isNotEmpty(ids)) {
            for (Long id : ids) {
                //根据id查询菜单
                MenuDto menuDto = menuService.findById(id);
                menuDtos.addAll(menuService.getSuperior(menuDto, new ArrayList<>()));
            }
            //构造菜单树
            List<MenuDto> menuTree = menuService.buildTree(new ArrayList<>(menuDtos));
            return new ResponseEntity<>(menuTree, HttpStatus.OK);
        }
        return new ResponseEntity<>(menuService.getMenus(null),HttpStatus.OK);
    }

    @Log("新增菜单")
    @ApiOperation("新增菜单")
    @PostMapping
    @PreAuthorize("@pl.check('menu:add')")
    public ResponseEntity<Object> create(@Validated @RequestBody Menu menu) {
        if (menu.getId() != null) {
            throw new BadRequestException("A new "+ ENTITY_NAME +" cannot already have an ID");
        }
        //创建菜单
        menuService.create(menu);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @Log("删除菜单")
    @ApiOperation("删除菜单")
    @DeleteMapping
    @PreAuthorize("@pl.check('menu:del')")
    public ResponseEntity<Object> delete(@RequestBody Set<Long> ids){
        //存放要删除的菜单集合
        Set<Menu> menuSet = new HashSet<>();
        for (Long id : ids) {
            //查询菜单下的子菜单列表
            List<MenuDto> menuList = menuService.getMenus(id);
            menuSet.add(menuService.findOne(id));
            //获取待删除的菜单
            menuSet = menuService.getDeleteMenus(menuMapper.toEntity(menuList), menuSet);
        }
        //批量删除
        menuService.delete(menuSet);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Log("编辑菜单")
    @ApiOperation("编辑菜单")
    @PutMapping
    @PreAuthorize("@pl.check('menu:edit')")
    public ResponseEntity<Object> update(@Validated(Menu.Update.class) @RequestBody Menu menu){
        menuService.update(menu);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Log("导出菜单数据")
    @ApiOperation("导出菜单数据")
    @GetMapping(value = "/download")
    @PreAuthorize("@pl.check('menu:list')")
    public void download(HttpServletResponse response, MenuQueryCriteria criteria) throws Exception {
        menuService.download(menuService.queryAll(criteria, false), response);
    }

}
