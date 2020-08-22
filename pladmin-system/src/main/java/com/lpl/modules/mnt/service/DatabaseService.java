package com.lpl.modules.mnt.service;

import com.lpl.modules.mnt.domain.Database;
import com.lpl.modules.mnt.service.dto.DatabaseDto;
import com.lpl.modules.mnt.service.dto.DatabaseQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 数据库业务接口
 */
public interface DatabaseService {

    /**
     * 分页查询数据库列表
     * @param criteria
     * @param pageable
     */
    Object queryAll(DatabaseQueryCriteria criteria, Pageable pageable);

    /**
     * 查询数据库，不分页
     * @param criteria
     */
    List<DatabaseDto> queryAll(DatabaseQueryCriteria criteria);

    /**
     * 新增数据库
     * @param database
     */
    void create(Database database);

    /**
     * 修改数据库
     * @param database
     */
    void update(Database database);

    /**
     * 删除数据库
     * @param ids
     */
    void delete(Set<String> ids);

    /**
     * 导出数据库
     * @param queryAll
     * @param response
     * @throws IOException
     */
    void download(List<DatabaseDto> queryAll, HttpServletResponse response) throws IOException;

    /**
     * 测试数据库连接
     * @param database
     */
    boolean testConnection(Database database);

    /**
     * 根据id查询数据库
     * @param id
     */
    DatabaseDto findById(String id);
}
