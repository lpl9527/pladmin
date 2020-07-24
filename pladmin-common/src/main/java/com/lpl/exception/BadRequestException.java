package com.lpl.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.BAD_REQUEST;


/**
 * @author lpl
 * 自定义错误请求异常
 */
public class BadRequestException extends RuntimeException{

    private Integer status = BAD_REQUEST.value();   //状态码

    public BadRequestException(String message) {
        super(message);
    }
    public BadRequestException(HttpStatus status, String message) {
        super(message);
        this.status = status.value();
    }
}
