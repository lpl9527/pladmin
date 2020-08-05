package com.lpl.modules.system.domain.vo;

import lombok.Data;

/**
 * @author lpl
 * 用户视图对象，用于修改密码
 */
@Data
public class UserPassVo {

    private String oldPass;

    private String newPass;
}
