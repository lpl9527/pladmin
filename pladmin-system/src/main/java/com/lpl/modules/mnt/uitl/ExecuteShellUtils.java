package com.lpl.modules.mnt.uitl;

import cn.hutool.core.io.IoUtil;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Vector;

/**
 * @author lpl
 * 执行shell脚本工具类
 */
@Slf4j
public class ExecuteShellUtils {

    private Vector<String> stdout;

    Session session;

    /**
     * 使用构造方法连接服务器
     * @param ipAddress ip地址
     * @param username  用户名
     * @param password  密码
     * @param port      端口
     */
    public ExecuteShellUtils(final String ipAddress, final String username, final String password,int port) {
        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, ipAddress, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect(3000);
        } catch (Exception e) {
            log.error(e.getMessage(),e);
        }
    }

    /**
     * 执行脚本，返回状态码
     * @param command  脚本命令
     */
    public int execute(final String command) {
        int returnCode = 0;
        ChannelShell channel = null;
        PrintWriter printWriter = null;
        BufferedReader input = null;
        stdout = new Vector<String>();
        try {
            channel = (ChannelShell) session.openChannel("shell");
            channel.connect();
            input = new BufferedReader(new InputStreamReader(channel.getInputStream()));
            printWriter = new PrintWriter(channel.getOutputStream());
            printWriter.println(command);
            printWriter.println("exit");
            printWriter.flush();
            log.info("The remote command is: ");
            String line;
            while ((line = input.readLine()) != null) {
                stdout.add(line);
                System.out.println(line);
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            return -1;
        }finally {
            IoUtil.close(printWriter);
            IoUtil.close(input);
            if (channel != null) {
                channel.disconnect();
            }
        }
        return returnCode;
    }

    /**
     * 关闭连接会话
     */
    public void close(){
        if (session != null) {
            session.disconnect();
        }
    }

    /**
     * 执行脚本并获取结果
     * @param command
     */
    public String executeForResult(String command) {
        execute(command);
        StringBuilder sb = new StringBuilder();
        for (String str : stdout) {
            sb.append(str);
        }
        return sb.toString();
    }
}
