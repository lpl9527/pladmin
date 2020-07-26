package com.lpl.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author lpl
 * Rsa配置属性类
 */
@Data
@Component
public class RsaProperties {

    public static String privateKey;    //私钥值

    @Value("${rsa.private_key}")
    public void setPrivateKey(String privateKey) {
        RsaProperties.privateKey = privateKey;
    }
}
