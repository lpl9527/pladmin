package com.lpl.service;

import com.lpl.domain.Log;
import com.lpl.service.dto.LogQueryCriteria;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author lpl
 * 日志业务层接口
 */
public interface LogService {

    /**
     * 保存日志数据，开启异步日志记录
     * @param username  用户名
     * @param browser   浏览器
     * @param ip    请求ip
     * @param joinPoint 连接点对象
     * @param log   日志对象
     */
    @Async
    void save(String username, String browser, String ip, ProceedingJoinPoint joinPoint, Log log);

    /**
     * 分页查询用户日志
     * @param criteria  查询条件
     * @param pageable  分页参数
     */
    Object queryAllByUser(LogQueryCriteria criteria, Pageable pageable);

    /**
     * 分页查询所有INFO类型日志
     * @param criteria
     * @param pageable
     */
    Object queryAll(LogQueryCriteria criteria, Pageable pageable);

    /**
     * 查询全部INFO类型数据，不分页
     * @param criteria
     */
    List<Log> queryAll(LogQueryCriteria criteria);

    /**
     * 日志异常详情查询
     * @param id
     */
    Object findByErrDetail(Long id);

    /**
     * 删除所有INFO日志
     */
    void delAllByInfo();

    /**
     * 删除所有INFO日志
     */
    void delAllByError();

    /**
     * 导出日志数据
     * @param logs
     * @param response
     * @throws IOException
     */
    void download(List<Log> logs, HttpServletResponse response) throws IOException;
}
