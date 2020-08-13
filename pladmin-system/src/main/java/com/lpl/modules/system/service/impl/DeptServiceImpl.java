package com.lpl.modules.system.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.lpl.modules.system.domain.Dept;
import com.lpl.modules.system.mapstruct.DeptMapper;
import com.lpl.modules.system.repository.DeptRepository;
import com.lpl.modules.system.service.DeptService;
import com.lpl.modules.system.service.dto.DeptDto;
import com.lpl.modules.system.service.dto.DeptQueryCriteria;
import com.lpl.utils.QueryHelp;
import com.lpl.utils.SecurityUtils;
import com.lpl.utils.StringUtils;
import com.lpl.utils.ValidationUtil;
import com.lpl.utils.enums.DataScopeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

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
