package com.lpl.modules.system.service.impl;

import com.lpl.modules.system.domain.Dept;
import com.lpl.modules.system.repository.DeptRepository;
import com.lpl.modules.system.service.DeptService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 部门Service实现类
 */
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "dept")
public class DeptServiceImpl implements DeptService {

    private final DeptRepository deptRepository;

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
}
