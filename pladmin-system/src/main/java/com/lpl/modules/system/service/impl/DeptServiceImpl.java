package com.lpl.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lpl.exception.BadRequestException;
import com.lpl.modules.system.domain.Dept;
import com.lpl.modules.system.domain.User;
import com.lpl.modules.system.mapstruct.DeptMapper;
import com.lpl.modules.system.repository.DeptRepository;
import com.lpl.modules.system.repository.RoleRepository;
import com.lpl.modules.system.repository.UserRepository;
import com.lpl.modules.system.service.DeptService;
import com.lpl.modules.system.service.dto.DeptDto;
import com.lpl.modules.system.service.dto.DeptQueryCriteria;
import com.lpl.utils.*;
import com.lpl.utils.enums.DataScopeEnum;
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
 * 部门Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dept")
public class DeptServiceImpl implements DeptService {

    private final DeptRepository deptRepository;
    private final DeptMapper deptMapper;
    private final RedisUtils redisUtils;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * 根据角色id查询具有操作权限的部门列表
     * @param roleId
     */
    @Override
    public Set<Dept> findByRoleId(Long roleId) {
        return deptRepository.findByRoleId(roleId);
    }

    /**
     * 根据父部门id查询部门列表
     * @param pid
     */
    @Override
    @Cacheable(key = "'pid:' + #p0")    //以pid: + 夫部门id作为缓存的key
    public List<Dept> findByPid(Long pid) {
        return deptRepository.findByPid(pid);
    }

    /**
     * 根据父部门id和此父部门下的部门列表递归查询所有部门层级下的部门id
     * @param deptPid
     * @param deptList
     */
    @Override
    public List<Long> getDeptChildren(Long deptPid, List<Dept> deptList) {
        //存放部门id
        List<Long> deptIds = new ArrayList<>();
        deptList.forEach(dept -> {
            if (null != dept && dept.getEnabled()) {
                //根据父部门id查询子部门列表
                List<Dept> depts = deptRepository.findByPid(dept.getId());
                if (deptList.size() != 0) {     //如果存在子部门
                    //再以此部门id和此部门子部门列表调用此方法尽心查询子部门列表
                    List<Long> deptChildren = getDeptChildren(dept.getId(), depts);
                    deptIds.addAll(deptChildren);   //将子部门id列表放入
                }
                deptIds.add(dept.getId());  //不管有没有子部门，都要讲当前部门id放入
            }
        });
        return deptIds;
    }

    /**
     * 查询所有部门数据
     * @param criteria
     * @param isQuery
     * @throws Exception
     */
    @Override
    public List<DeptDto> queryAll(DeptQueryCriteria criteria, Boolean isQuery) throws Exception {
        //指定排序字段
        Sort sort = new Sort(Sort.Direction.ASC, "deptSort");
        String dataScopeType = SecurityUtils.getDataScopeType();
        if (isQuery) {
            if (dataScopeType.equals(DataScopeEnum.ALL.getValue())) {
                criteria.setPidIsNull(true);
            }

            //增加数据权限过滤属性（即查询条件）
            List<String> fieldNames = new ArrayList<String>(){{
                add("pidIsNull");
                add("enabled");
            }};
            //获取部门查询条件的所有Field属性对象列表
            List<Field> fields = QueryHelp.getAllFields(criteria.getClass(), new ArrayList<>());
            for (Field field : fields) {
                //强制反射，保证对private属性的访问
                field.setAccessible(true);
                Object val = field.get(criteria);
                if (fieldNames.contains(field.getName())) {
                    continue;
                }
                if (ObjectUtil.isNotNull(val)) {
                    criteria.setPidIsNull(null);
                    break;
                }
            }
        }
        //查询部门列表
        List<DeptDto> list = deptMapper.toDto(deptRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder), sort));
        // 如果为空，就代表为自定义权限或者本级权限，就需要去重，不理解可以注释掉，看查询结果
        if(StringUtils.isBlank(dataScopeType)){
            return deduplication(list);
        }
        return list;
    }

    /**
     * 根据id查询部门
     * @param id
     */
    @Override
    @Cacheable(key = "'id:' + #p0")
    public DeptDto findById(Long id) {
        //根据id查询部门
        Dept dept = deptRepository.findById(id).orElseGet(Dept::new);
        ValidationUtil.isNull(dept.getId(),"Dept","id",id);
        return deptMapper.toDto(dept);
    }

    /**
     * 新增部门
     * @param dept
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Dept dept) {
        //保存
        deptRepository.save(dept);
        //设置子节点个数为0
        dept.setSubCount(0);
        //清理缓存
        redisUtils.del("dept::pid:" + (dept.getPid() == null ? 0 : dept.getPid()));
        //更新子节点数量
        updateSubCnt(dept.getPid());
    }

    /**
     * 编辑部门
     * @param resources
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Dept resources) {
        //获取旧部门父id
        Long oldPid = findById(resources.getId()).getPid();
        //获取新的父部门id
        Long newPid = resources.getPid();
        if(resources.getPid() != null && resources.getId().equals(resources.getPid())) {
            throw new BadRequestException("上级不能为自己");
        }
        Dept dept = deptRepository.findById(resources.getId()).orElseGet(Dept::new);
        ValidationUtil.isNull( dept.getId(),"Dept","id",resources.getId());
        resources.setId(dept.getId());
        //保存部门
        deptRepository.save(resources);
        //更新父部门的子节点数目
        updateSubCnt(oldPid);
        updateSubCnt(newPid);
        //清理缓存
        delCaches(resources.getId(), oldPid, newPid);
    }

    /**
     * 批量删除部门
     * @param deptDtos
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<DeptDto> deptDtos) {
        for (DeptDto deptDto : deptDtos) {
            // 清理缓存
            delCaches(deptDto.getId(), deptDto.getPid(), null);
            //根据id删除部门
            deptRepository.deleteById(deptDto.getId());
            //更新所在部门子部门数量
            updateSubCnt(deptDto.getPid());
        }
    }

    /**
     * 获取待删除的部门集合
     * @param deptList
     * @param deptDtos
     */
    @Override
    public Set<DeptDto> getDeleteDepts(List<Dept> deptList, Set<DeptDto> deptDtos) {
        for (Dept dept : deptList) {
            deptDtos.add(deptMapper.toDto(dept));
            List<Dept> depts = deptRepository.findByPid(dept.getId());
            if(depts!=null && depts.size()!=0){
                //递归获取待删除部门集合
                getDeleteDepts(depts, deptDtos);
            }
        }
        return deptDtos;
    }

    /**
     * 更新部门子节点数量
     * @param deptId
     */
    private void updateSubCnt(Long deptId) {
        if (null != deptId) {
            //根据部门id查询子节点数量
            int count = deptRepository.countByPid(deptId);
            //更新子节点数量
            deptRepository.updateSubCntById(count, deptId);
        }
    }

    public void delCaches(Long id, Long oldPid, Long newPid) {
        //根据部门id查询用户列表
        List<User> users = userRepository.findByDeptId(id);
        //删除相关数据权限缓存
        redisUtils.delByKeys("data::user:",users.stream().map(User::getId).collect(Collectors.toSet()));
        redisUtils.del("dept::id:" + id);
        redisUtils.del("dept::pid:" + (oldPid == null ? 0 : oldPid));
        redisUtils.del("dept::pid:" + (newPid == null ? 0 : newPid));
    }

    /**
     * 递归查询部门上级部门列表
     * @param deptDto 本级部门
     * @param depts 上级部门列表
     */
    @Override
    public List<DeptDto> getSuperior(DeptDto deptDto, List<Dept> depts) {
        //如当前部门没有上级部门，则为顶级部门，获取，返回
        if (deptDto.getPid() == null) {
            //获取顶级部门
            depts.addAll(deptRepository.findByPidIsNull());
            return deptMapper.toDto(depts);
        }
        depts.addAll(deptRepository.findByPid(deptDto.getPid()));
        return getSuperior(findById(deptDto.getPid()), depts);
    }

    /**
     * 构造树形部门数据
     * @param deptDtos
     */
    @Override
    public Object buildTree(List<DeptDto> deptDtos) {
        Set<DeptDto> trees = new LinkedHashSet<>();
        Set<DeptDto> depts= new LinkedHashSet<>();
        List<String> deptNames = deptDtos.stream().map(DeptDto::getName).collect(Collectors.toList());
        boolean isChild;
        for (DeptDto deptDTO : deptDtos) {
            isChild = false;
            if (deptDTO.getPid() == null) {
                trees.add(deptDTO);
            }
            for (DeptDto it : deptDtos) {
                if (it.getPid() != null && deptDTO.getId().equals(it.getPid())) {
                    isChild = true;
                    if (deptDTO.getChildren() == null) {
                        deptDTO.setChildren(new ArrayList<>());
                    }
                    deptDTO.getChildren().add(it);
                }
            }
            if(isChild) {
                depts.add(deptDTO);
            } else if(deptDTO.getPid() != null &&  !deptNames.contains(findById(deptDTO.getPid()).getName())) {
                depts.add(deptDTO);
            }
        }

        if (CollectionUtil.isEmpty(trees)) {
            trees = depts;
        }
        Map<String,Object> map = new HashMap<>(2);
        map.put("totalElements",deptDtos.size());
        map.put("content",CollectionUtil.isEmpty(trees)? deptDtos :trees);
        return map;
    }

    /**
     * 验证部门是否被角色或者用户关联
     * @param deptDtos
     */
    @Override
    public void verification(Set<DeptDto> deptDtos) {
       //获取部门列表集合
        Set<Long> deptIds = deptDtos.stream().map(DeptDto::getId).collect(Collectors.toSet());
        if(userRepository.countByDepts(deptIds) > 0){
            throw new BadRequestException("所选部门存在用户关联，请解除后再试！");
        }
        if(roleRepository.countByDepts(deptIds) > 0){
            throw new BadRequestException("所选部门存在角色关联，请解除后再试！");
        }
    }

    /**
     * 导出部门数据
     * @param deptDtos 待导出数据
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<DeptDto> deptDtos, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DeptDto deptDTO : deptDtos) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("部门名称", deptDTO.getName());
            map.put("部门状态", deptDTO.getEnabled() ? "启用" : "停用");
            map.put("创建日期", deptDTO.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 部门去重
     * @param list
     */
    private List<DeptDto> deduplication(List<DeptDto> list) {
        List<DeptDto> deptDtos = new ArrayList<>();
        for (DeptDto deptDto : list) {
            boolean flag = true;
            for (DeptDto dto : list) {
                if (deptDto.getPid().equals(dto.getId())) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                deptDtos.add(deptDto);
            }
        }
        return deptDtos;
    }
}
