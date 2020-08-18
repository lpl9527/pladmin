package com.lpl.modules.system.service;

import java.util.Map;

/**
 * @author lpl
 * 监控业务接口
 */
public interface MonitorService {

    /**
     * 查询系统信息
     */
    Map<String, Object> getServers();
}
