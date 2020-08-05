package com.lpl.utils;

import cn.hutool.core.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lpl
 * File工具类，扩展hutool工具包
 */
public class FileUtils extends FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 文件上传
     * @param file  MultipartFile文件
     * @param filePath  文件路径
     */
    public static File upload(MultipartFile file, String filePath) {
        //增加时间作为文件名
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddhhmmssS");
        String nowStr = "-" + sdf.format(date);
        //获取不带扩展名的文件名
        String name = getFileNameNoEx(file.getOriginalFilename());
        //获取文件扩展名
        String suffix = getExtensionName(file.getOriginalFilename());
        try{
            //构建文件全路径
            String fileName = name + nowStr + "." + suffix;
            String path = filePath + fileName;

            File destination = new File(path).getCanonicalFile();
            //检测是否存在目录
            if (!destination.getParentFile().exists()) {
                if (!destination.getParentFile().mkdirs()) {   //不存在就创建目录
                    System.err.println("文件夹创建失败！");
                }
            }
            //文件写入
            file.transferTo(destination);

            return destination;
        }catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取不带扩展名的文件名
     * @param fileName
     */
    public static String getFileNameNoEx(String fileName) {
        if (null != fileName && fileName.length() > 0) {
            int dot = fileName.lastIndexOf('.');
            if (dot > -1 && dot < fileName.length()) {
                return fileName.substring(0, dot);
            }
        }
        return fileName;
    }

    /**
     * 获取文件扩展名，不带 .
     * @param fileName  文件名称
     */
    public static String getExtensionName(String fileName) {
        if (null != fileName && fileName.length() > 0) {
            int dot = fileName.lastIndexOf('.');
            if (dot > -1 && dot < fileName.length()-1) {
                return fileName.substring(dot + 1);
            }
        }
        return fileName;
    }
}
