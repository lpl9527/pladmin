package com.lpl.utils;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.poi.excel.BigExcelWriter;
import cn.hutool.poi.excel.ExcelUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lpl
 * File工具类，扩展hutool工具包
 */
public class FileUtils extends FileUtil {

    private static final Logger log = LoggerFactory.getLogger(FileUtil.class);

    /**
     * 系统临时目录
     * <pre>
     *     java.io.tmpdir
     *     windows : C:\Users/xxx\AppData\Local\Temp\
     *     linux: /temp
     * </pre>
     */
    public static final String SYS_TEM_DIR = System.getProperty("java.io.tmpdir") + File.separator;

    /**
     * 定义GB的计算常量
     */
    private static final int GB = 1024 * 1024 * 1024;
    /**
     * 定义MB的计算常量
     */
    private static final int MB = 1024 * 1024;
    /**
     * 定义KB的计算常量
     */
    private static final int KB = 1024;

    /**
     * 格式化小数
     */
    private static final DecimalFormat DF = new DecimalFormat("0.00");

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

    public static void downloadExcel(List<Map<String, Object>> list, HttpServletResponse response) throws IOException {
        String tempPath = SYS_TEM_DIR + IdUtil.fastSimpleUUID() + ".xlsx";
        File file = new File(tempPath);
        BigExcelWriter writer = ExcelUtil.getBigWriter(file);
        // 一次性写出内容，使用默认样式，强制输出标题
        writer.write(list, true);
        //response为HttpServletResponse对象
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
        response.setHeader("Content-Disposition", "attachment;filename=file.xlsx");
        ServletOutputStream out = response.getOutputStream();
        // 终止后删除临时文件
        file.deleteOnExit();
        writer.flush(out, true);
        //此处记得关闭输出Servlet流
        IoUtil.close(out);
    }
}
