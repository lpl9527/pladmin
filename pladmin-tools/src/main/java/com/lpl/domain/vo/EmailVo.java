package com.lpl.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @author lpl
 * 邮件视图对象，发送邮件时，接收参数的类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVo {

    /**
     * 收件人，支持多个收件人
     */
    @NotEmpty
    private List<String> tos;

    /**
     * 邮件主题
     */
    @NotBlank
    private String subject;

    /**
     * 邮件内容
     */
    @NotBlank
    private String content;
}
