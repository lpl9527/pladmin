package com.lpl.config;

import com.lpl.utils.AppConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author lpl
 * 文件上传配置项
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private Long maxSize;   //文件大小限制

    private Long avatarMaxSize;     //头像大小限制

    private PlPath mac;

    private PlPath linux;

    private PlPath windows;

    /**
     * 获取文件路径和头像路径对象
     */
    public PlPath getPath() {
        //获取操作系统名称
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith(AppConstant.WIN)) {
            return windows;
        }else if(os.toLowerCase().startsWith(AppConstant.MAC)) {
            return mac;
        }
        return linux;
    }


    @Data
    public static class PlPath{

        private String path;    //文件路径

        private String avatar;  //头像路径
    }
}

