package com.lpl.modules.mnt.uitl;

import lombok.extern.slf4j.Slf4j;

/**
 * @author lpl
 * 数据库类型枚举
 */
@Slf4j
public enum  DatabaseTypeEnum {

    /** mysql */
    MYSQL("mysql", "mysql", "com.mysql.jdbc.Driver", "`", "`", "'", "'"),

    /** oracle */
    ORACLE("oracle", "oracle", "oracle.jdbc.driver.OracleDriver", "\"", "\"", "\"", "\""),

    /** sql server */
    SQLSERVER("sqlserver", "sqlserver", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "\"", "\"", "\"", "\""),

    /** h2 */
    H2("h2", "h2", "org.h2.Driver", "`", "`", "\"", "\""),

    /** phoenix */
    PHOENIX("phoenix", "hbase phoenix", "org.apache.phoenix.jdbc.PhoenixDriver", "", "", "\"", "\""),

    /** mongo */
    MONGODB("mongo", "mongodb", "mongodb.jdbc.MongoDriver", "`", "`", "\"", "\""),

    /** sql4es */
    ELASTICSEARCH("sql4es", "elasticsearch", "nl.anchormen.sql4es.jdbc.ESDriver", "", "", "'", "'"),

    /** presto */
    PRESTO("presto", "presto", "com.facebook.presto.jdbc.PrestoDriver", "", "", "\"", "\""),

    /** moonbox */
    MOONBOX("moonbox", "moonbox", "moonbox.jdbc.MbDriver", "`", "`", "`", "`"),

    /** cassandra */
    CASSANDRA("cassandra", "cassandra", "com.github.adejanovski.cassandra.jdbc.CassandraDriver", "", "", "'", "'"),

    /** click house */
    CLICKHOUSE("clickhouse", "clickhouse", "ru.yandex.clickhouse.ClickHouseDriver", "", "", "\"", "\""),

    /** kylin */
    KYLIN("kylin", "kylin", "org.apache.kylin.jdbc.Driver", "\"", "\"", "\"", "\""),

    /** vertica */
    VERTICA("vertica", "vertica", "com.vertica.jdbc.Driver", "", "", "'", "'"),

    /** sap */
    HANA("sap", "sap hana", "com.sap.db.jdbc.Driver", "", "", "'", "'"),

    /** impala */
    IMPALA("impala", "impala", "com.cloudera.impala.jdbc41.Driver", "", "", "'", "'");

    private String feature;
    private String desc;
    private String driver;
    private String keywordPrefix;
    private String keywordSuffix;
    private String aliasPrefix;
    private String aliasSuffix;

    private static final String JDBC_URL_PREFIX = "jdbc:";

    DatabaseTypeEnum(String feature, String desc, String driver, String keywordPrefix, String keywordSuffix, String aliasPrefix, String aliasSuffix) {
        this.feature = feature;
        this.desc = desc;
        this.driver = driver;
        this.keywordPrefix = keywordPrefix;
        this.keywordSuffix = keywordSuffix;
        this.aliasPrefix = aliasPrefix;
        this.aliasSuffix = aliasSuffix;
    }

    /**
     * 根据数据库地址判断返回数据库类型枚举
     * @param jdbcUrl
     */
    public static DatabaseTypeEnum urlOf(String jdbcUrl) {
        String url = jdbcUrl.toLowerCase().trim();
        for (DatabaseTypeEnum databaseTypeEnum : values()) {
            if (url.startsWith(JDBC_URL_PREFIX + databaseTypeEnum.feature)) {
                try {
                    Class<?> aClass = Class.forName(databaseTypeEnum.getDriver());
                    if (null == aClass) {
                        throw new RuntimeException("Unable to get driver instance for jdbcUrl: " + jdbcUrl);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException("Unable to get driver instance: " + jdbcUrl);
                }
                return databaseTypeEnum;
            }
        }
        return null;
    }

    public String getFeature() {
        return feature;
    }

    public String getDesc() {
        return desc;
    }

    public String getDriver() {
        return driver;
    }

    public String getKeywordPrefix() {
        return keywordPrefix;
    }

    public String getKeywordSuffix() {
        return keywordSuffix;
    }

    public String getAliasPrefix() {
        return aliasPrefix;
    }

    public String getAliasSuffix() {
        return aliasSuffix;
    }
}
