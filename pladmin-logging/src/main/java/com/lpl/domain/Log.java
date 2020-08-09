package com.lpl.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author lpl
 * 系统日志实体类
 */
@Entity
@Getter
@Setter
@Table(name = "sys_log")
@NoArgsConstructor
public class Log implements Serializable {

    @Id
    @Column(name = "log_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;   //日志主键id

    private String username;    //操作用户名

    private String description;     //描述

    private String method;      //方法名

    private String params;   //请求参数

    private String logType;     //日志类型

    private String requestIp;   //请求IP

    private String address;     //请求地址

    private String browser;     //浏览器

    private Long time;      //请求耗时

    private byte[] exceptionDetail;     //异常详细

    @CreationTimestamp
    private Timestamp createTime;   //创建日期

    public Log(String logType, Long time) {
        this.logType = logType;
        this.time = time;
    }
}
