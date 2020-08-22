package com.lpl.modules.mnt.service.impl;

import cn.hutool.core.util.IdUtil;
import com.lpl.modules.mnt.domain.Database;
import com.lpl.modules.mnt.repository.DatabaseRepository;
import com.lpl.modules.mnt.service.DatabaseService;
import com.lpl.modules.mnt.service.dto.DatabaseDto;
import com.lpl.modules.mnt.service.dto.DatabaseQueryCriteria;
import com.lpl.modules.mnt.service.mapstruct.DatabaseMapper;
import com.lpl.modules.mnt.uitl.SqlUtils;
import com.lpl.utils.FileUtils;
import com.lpl.utils.PageUtil;
import com.lpl.utils.QueryHelp;
import com.lpl.utils.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

/**
 * @author lpl
 * 数据库业务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DatabaseServiceImpl implements DatabaseService {

    private final DatabaseRepository databaseRepository;
    private final DatabaseMapper databaseMapper;

    /**
     * 分页查询数据库列表
     * @param criteria
     * @param pageable
     */
    @Override
    public Object queryAll(DatabaseQueryCriteria criteria, Pageable pageable) {
        Page<Database> page = databaseRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder),pageable);
        return PageUtil.toPage(page.map(databaseMapper::toDto));
    }

    /**
     * 查询数据库，不分页
     * @param criteria
     */
    @Override
    public List<DatabaseDto> queryAll(DatabaseQueryCriteria criteria) {
        return databaseMapper.toDto(databaseRepository.findAll((root, criteriaQuery, criteriaBuilder) -> QueryHelp.getPredicate(root,criteria,criteriaBuilder)));
    }

    /**
     * 新增数据库
     * @param database
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void create(Database database) {
        database.setId(IdUtil.simpleUUID());
        databaseRepository.save(database);
    }

    /**
     * 修改数据库
     * @param database
     */
    @Override
    public void update(Database database) {
        Database oldDatabase = databaseRepository.findById(database.getId()).orElseGet(Database::new);
        ValidationUtil.isNull(oldDatabase.getId(),"Database","id",database.getId());

        oldDatabase.copy(database);
        databaseRepository.save(oldDatabase);
    }

    /**
     * 删除数据库
     * @param ids
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Set<String> ids) {
        for (String id : ids) {
            databaseRepository.deleteById(id);
        }
    }

    /**
     * 导出数据库
     * @param queryAll
     * @param response
     * @throws IOException
     */
    @Override
    public void download(List<DatabaseDto> queryAll, HttpServletResponse response) throws IOException {
        List<Map<String, Object>> list = new ArrayList<>();
        for (DatabaseDto databaseDto : queryAll) {
            Map<String,Object> map = new LinkedHashMap<>();
            map.put("数据库名称", databaseDto.getName());
            map.put("数据库连接地址", databaseDto.getJdbcUrl());
            map.put("用户名", databaseDto.getUserName());
            map.put("创建日期", databaseDto.getCreateTime());
            list.add(map);
        }
        FileUtils.downloadExcel(list, response);
    }

    /**
     * 测试数据库连接
     * @param database
     */
    @Override
    public boolean testConnection(Database database) {
        try {
            //测试数据库连接
            return SqlUtils.testConnection(database.getJdbcUrl(), database.getUserName(), database.getPwd());
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
    }

    /**
     * 根据id查询数据库
     * @param id
     */
    @Override
    public DatabaseDto findById(String id) {
        Database database = databaseRepository.findById(id).orElseGet(Database::new);
        ValidationUtil.isNull(database.getId(),"Database","id",id);

        return databaseMapper.toDto(database);
    }
}
