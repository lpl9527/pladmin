package com.lpl.modules.system.service.impl;

import cn.hutool.core.date.BetweenFormater;
import cn.hutool.core.date.DateUtil;
import com.lpl.modules.system.service.MonitorService;
import com.lpl.utils.FileUtils;
import com.lpl.utils.StringUtils;
import org.springframework.stereotype.Service;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.HardwareAbstractionLayer;
import oshi.software.os.FileSystem;
import oshi.software.os.OSFileStore;
import oshi.software.os.OperatingSystem;
import oshi.util.FormatUtil;
import oshi.util.Util;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @author lpl
 * 监控业务实现类
 */
@Service
public class MonitorServiceImpl implements MonitorService {

    private final DecimalFormat df = new DecimalFormat("0.00");

    /**
     * 查询系统信息
     */
    @Override
    public Map<String, Object> getServers() {

        Map<String, Object> resultMap = new LinkedHashMap<>(8);
        try {
            SystemInfo systemInfo = new SystemInfo();
            //获取操作系统
            OperatingSystem os = systemInfo.getOperatingSystem();
            //获取硬盘
            HardwareAbstractionLayer hal = systemInfo.getHardware();

            //-------------------------------------
            //系统信息
            resultMap.put("sys", getSystemInfo(os));
            //cpu信息
            resultMap.put("cpu", getCpuInfo(hal.getProcessor()));
            // 内存信息
            resultMap.put("memory", getMemoryInfo(hal.getMemory()));
            // 交换区信息
            resultMap.put("swap", getSwapInfo(hal.getMemory()));
            // 磁盘信息
            resultMap.put("disk", getDiskInfo(os));
            //当前时间（时分秒格式）
            resultMap.put("time", DateUtil.format(new Date(), "HH:mm:ss"));
        }catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    /**
     * 获取系统相关信息，系统名称、运行天数、系统IP
     * @param os
     */
    private Map<String, Object> getSystemInfo(OperatingSystem os) {
        Map<String,Object> systemInfo = new LinkedHashMap<>();
        // jvm 运行时间
        long time = ManagementFactory.getRuntimeMXBean().getStartTime();
        Date date = new Date(time);
        // 计算项目运行时间
        String formatBetween = DateUtil.formatBetween(date, new Date(), BetweenFormater.Level.HOUR);
        // 系统信息
        systemInfo.put("os", os.toString());
        systemInfo.put("day", formatBetween);
        systemInfo.put("ip", StringUtils.getLocalIp());

        return systemInfo;
    }

    /**
     * 获取cpu相关信息
     * @param processor
     */
    private Map<String, Object> getCpuInfo(CentralProcessor processor) {
        Map<String,Object> cpuInfo = new LinkedHashMap<>();
        cpuInfo.put("name", processor.getProcessorIdentifier().getName());
        cpuInfo.put("package", processor.getPhysicalPackageCount() + "个物理CPU");
        cpuInfo.put("core", processor.getPhysicalProcessorCount() + "个物理核心");
        cpuInfo.put("coreNumber", processor.getPhysicalProcessorCount());
        cpuInfo.put("logic", processor.getLogicalProcessorCount() + "个逻辑CPU");
        // CPU信息
        long[] prevTicks = processor.getSystemCpuLoadTicks();
        // 等待1秒...
        Util.sleep(1000);
        long[] ticks = processor.getSystemCpuLoadTicks();
        long user = ticks[CentralProcessor.TickType.USER.getIndex()] - prevTicks[CentralProcessor.TickType.USER.getIndex()];
        long nice = ticks[CentralProcessor.TickType.NICE.getIndex()] - prevTicks[CentralProcessor.TickType.NICE.getIndex()];
        long sys = ticks[CentralProcessor.TickType.SYSTEM.getIndex()] - prevTicks[CentralProcessor.TickType.SYSTEM.getIndex()];
        long idle = ticks[CentralProcessor.TickType.IDLE.getIndex()] - prevTicks[CentralProcessor.TickType.IDLE.getIndex()];
        long iowait = ticks[CentralProcessor.TickType.IOWAIT.getIndex()] - prevTicks[CentralProcessor.TickType.IOWAIT.getIndex()];
        long irq = ticks[CentralProcessor.TickType.IRQ.getIndex()] - prevTicks[CentralProcessor.TickType.IRQ.getIndex()];
        long softirq = ticks[CentralProcessor.TickType.SOFTIRQ.getIndex()] - prevTicks[CentralProcessor.TickType.SOFTIRQ.getIndex()];
        long steal = ticks[CentralProcessor.TickType.STEAL.getIndex()] - prevTicks[CentralProcessor.TickType.STEAL.getIndex()];
        long totalCpu = user + nice + sys + idle + iowait + irq + softirq + steal;
        cpuInfo.put("used", df.format(100d * user / totalCpu + 100d * sys / totalCpu));
        cpuInfo.put("idle", df.format(100d * idle / totalCpu));
        return cpuInfo;
    }

    /**
     * 获取内存相关信息
     * @param memory
     */
    private Map<String,Object> getMemoryInfo(GlobalMemory memory) {
        Map<String,Object> memoryInfo = new LinkedHashMap<>();
        memoryInfo.put("total", FormatUtil.formatBytes(memory.getTotal()));
        memoryInfo.put("available", FormatUtil.formatBytes(memory.getAvailable()));
        memoryInfo.put("used", FormatUtil.formatBytes(memory.getTotal() - memory.getAvailable()));
        memoryInfo.put("usageRate", df.format((memory.getTotal() - memory.getAvailable())/(double)memory.getTotal() * 100));
        return memoryInfo;
    }

    /**
     * 获取交换区相关信息
     * @param memory
     */
    private Map<String,Object> getSwapInfo(GlobalMemory memory) {
        Map<String,Object> swapInfo = new LinkedHashMap<>();
        swapInfo.put("total", FormatUtil.formatBytes(memory.getVirtualMemory().getSwapTotal()));
        swapInfo.put("used", FormatUtil.formatBytes(memory.getVirtualMemory().getSwapUsed()));
        swapInfo.put("available", FormatUtil.formatBytes(memory.getVirtualMemory().getSwapTotal() - memory.getVirtualMemory().getSwapUsed()));
        swapInfo.put("usageRate", df.format(memory.getVirtualMemory().getSwapUsed()/(double)memory.getVirtualMemory().getSwapTotal() * 100));
        return swapInfo;
    }

    /**
     * 获取磁盘相关信息
     * @param os
     */
    private Map<String,Object> getDiskInfo(OperatingSystem os) {
        Map<String,Object> diskInfo = new LinkedHashMap<>();
        FileSystem fileSystem = os.getFileSystem();
        List<OSFileStore> fsArray = fileSystem.getFileStores();
        for (OSFileStore fs : fsArray){
            diskInfo.put("total", fs.getTotalSpace() > 0 ? FileUtils.getSize(fs.getTotalSpace()) : "?");
            long used = fs.getTotalSpace() - fs.getUsableSpace();
            diskInfo.put("available", FileUtils.getSize(fs.getUsableSpace()));
            diskInfo.put("used", FileUtils.getSize(used));
            diskInfo.put("usageRate", df.format(used/(double)fs.getTotalSpace() * 100));
        }
        return diskInfo;
    }
}
