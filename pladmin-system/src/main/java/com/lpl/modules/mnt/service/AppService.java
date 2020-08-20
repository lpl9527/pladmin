package com.lpl.modules.mnt.service;

import com.lpl.modules.mnt.domain.App;
import com.lpl.modules.mnt.service.dto.AppDto;
import com.lpl.modules.mnt.service.dto.AppQueryCriteria;
import org.springframework.data.domain.Pageable;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * @author lpl
 * 应用业务接口
 */
public interface AppService {

    /**
     * 分页查询应用
     * @param criteria
     * @param pageable
     */
    Object queryAll(AppQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部数据
     * @param criteria
     */
    List<AppDto> queryAll(AppQueryCriteria criteria);

    /**
     * 创建应用
     * @param app
     */
    void create(App app);

    /**
     * 更新应用
     * @param app
     */
    void update(App app);

    /**
     * 删除应用
     * @param ids
     */
    void delete(Set<Long> ids);

    /**
     * 导出应用到表格
     * @param queryAll
     * @param response
     * @throws IOException
     */
    void download(List<AppDto> queryAll, HttpServletResponse response) throws IOException;
}
