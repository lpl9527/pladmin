package com.lpl.modules.mnt.uitl;

import cn.hutool.crypto.SecureUtil;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lpl
 * 数据库sql工具类
 */
@Slf4j
public class SqlUtils {

    public static final String COLON = ":";
    private static volatile Map<String, DruidDataSource> map = new HashMap<>();     //放置数据源

    /**
     * 测试数据库连接
     * @param jdbcUrl 地址
     * @param userName 用户名
     * @param password 密码
     */
    public static boolean testConnection(String jdbcUrl, String userName, String password) {
        Connection connection = null;
        try {
            //获取数据库连接
            connection = getConnection(jdbcUrl, userName, password);
            if (null != connection) {
                return true;
            }
        } catch (Exception e) {
            log.info("Get connection failed:" + e.getMessage());
        } finally {
            //释放连接
            releaseConnection(connection);
        }
        return false;
    }

    /**
     * 获取数据库连接
     * @param jdbcUrl 地址
     * @param userName 用户名
     * @param password 密码
     */
    private static Connection getConnection(String jdbcUrl, String userName, String password) {
        DataSource dataSource = getDataSource(jdbcUrl, userName, password);
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
        } catch (Exception ignored) {}
        try {
            int timeOut = 5;
            if (null == connection || connection.isClosed() || !connection.isValid(timeOut)) {
                log.info("connection is closed or invalid, retry get connection!");
                connection = dataSource.getConnection();
            }
        } catch (Exception e) {
            log.error("create connection error, jdbcUrl: {}", jdbcUrl);
            throw new RuntimeException("create connection error, jdbcUrl: " + jdbcUrl);
        }
        return connection;
    }

    /**
     * 获取数据源
     * @param jdbcUrl 地址
     * @param userName 用户名
     * @param password 密码
     */
    private static DataSource getDataSource(String jdbcUrl, String userName, String password) {
        //获取数据库连接字符串
        String key = getKey(jdbcUrl, userName, password);
        if (!map.containsKey(key) || null == map.get(key)) {
            DruidDataSource druidDataSource = new DruidDataSource();
            String className;
            try {
                className = DriverManager.getDriver(jdbcUrl.trim()).getClass().getName();
            } catch (SQLException e) {
                throw new RuntimeException("Get class name error: =" + jdbcUrl);
            }
            //根据ClassName获取数据库类型枚举
            if (StringUtils.isEmpty(className)) {
                DatabaseTypeEnum dataTypeEnum = DatabaseTypeEnum.urlOf(jdbcUrl);
                if (null == dataTypeEnum) {
                    throw new RuntimeException("Not supported data type: jdbcUrl=" + jdbcUrl);
                }
                druidDataSource.setDriverClassName(dataTypeEnum.getDriver());
            } else {
                druidDataSource.setDriverClassName(className);
            }
            //设置基本信息
            druidDataSource.setUrl(jdbcUrl);
            druidDataSource.setUsername(userName);
            druidDataSource.setPassword(password);

            // 配置获取连接等待超时的时间
            druidDataSource.setMaxWait(3000);
            // 配置初始化大小、最小、最大
            druidDataSource.setInitialSize(1);
            druidDataSource.setMinIdle(1);
            druidDataSource.setMaxActive(1);

            // 配置间隔多久才进行一次检测需要关闭的空闲连接，单位是毫秒
            druidDataSource.setTimeBetweenEvictionRunsMillis(50000);
            // 配置一旦重试多次失败后等待多久再继续重试连接，单位是毫秒
            druidDataSource.setTimeBetweenConnectErrorMillis(18000);
            // 配置一个连接在池中最小生存的时间，单位是毫秒
            druidDataSource.setMinEvictableIdleTimeMillis(300000);
            // 这个特性能解决 MySQL 服务器8小时关闭连接的问题
            druidDataSource.setMaxEvictableIdleTimeMillis(25200000);

            try {
                druidDataSource.init();
            } catch (SQLException e) {
                log.error("Exception during pool initialization", e);
                throw new RuntimeException(e.getMessage());
            }
            map.put(key, druidDataSource);
        }
        return map.get(key);
    }

    /**
     * 拼接数据库连接地址
     * @param jdbcUrl 地址
     * @param username 用户名
     * @param password 密码
     */
    private static String getKey(String jdbcUrl, String username, String password) {
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isEmpty(username)) {
            sb.append(username);
        }
        if (!StringUtils.isEmpty(password)) {
            sb.append(COLON).append(password);
        }
        sb.append(COLON).append(jdbcUrl.trim());

        return SecureUtil.md5(sb.toString());
    }

    /**
     * 释放数据库连接
     * @param connection
     */
    private static void releaseConnection(Connection connection) {
        if (null != connection) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error(e.getMessage(),e);
                log.error("connection close error：" + e.getMessage());
            }
        }
    }

    /**
     * 执行sql脚本
     * @param jdbcUrl 数据库地址
     * @param userName 用户名
     * @param password 密码
     * @param sqlFile 脚本文件
     */
    public static String executeFile(String jdbcUrl, String userName, String password, File sqlFile) {
        Connection connection = getConnection(jdbcUrl, userName, password);
        try {
            batchExecute(connection, readSqlList(sqlFile));
        } catch (Exception e) {
            log.error("sql脚本执行发生异常:{}",e.getMessage());
            return e.getMessage();
        }finally {
            releaseConnection(connection);
        }
        return "success";
    }

    /**
     * 批量执行sql
     * @param connection
     * @param sqlList
     * @throws SQLException
     */
    public static void batchExecute(Connection connection, List<String> sqlList) throws SQLException {
        Statement st = connection.createStatement();
        for (String sql : sqlList) {
            if (sql.endsWith(";")) {
                sql = sql.substring(0, sql.length() - 1);
            }
            st.addBatch(sql);
        }
        //批量执行
        st.executeBatch();
    }

    /**
     * 将文件中的sql语句以 ; 分割为单位读取到列表中
     * @param sqlFile sql脚本
     * @throws Exception
     */
    private static List<String> readSqlList(File sqlFile) throws Exception {
        List<String> sqlList = Lists.newArrayList();
        StringBuilder sb = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(sqlFile), StandardCharsets.UTF_8))) {
            String tmp;
            while ((tmp = reader.readLine()) != null) {
                log.info("line:{}", tmp);
                if (tmp.endsWith(";")) {
                    sb.append(tmp);
                    sqlList.add(sb.toString());
                    sb.delete(0, sb.length());
                } else {
                    sb.append(tmp);
                }
            }
            if (!"".endsWith(sb.toString().trim())) {
                sqlList.add(sb.toString());
            }
        }
        return sqlList;
    }
}
