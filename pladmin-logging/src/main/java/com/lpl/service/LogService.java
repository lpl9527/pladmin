package com.lpl.service;

import com.lpl.domain.Log;
import com.lpl.service.dto.LogQueryCriteria;
import org.aspectj.lang.ProceedingJoinPoint;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;

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
}
