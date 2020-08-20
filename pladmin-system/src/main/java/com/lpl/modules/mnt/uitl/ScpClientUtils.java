package com.lpl.modules.mnt.uitl;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import com.google.common.collect.Maps;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author lpl
 * 用于远程执行linux命令工具类
 */
public class ScpClientUtils {

    private String ip;
    private int port;
    private String username;
    private String password;

    static private Map<String,ScpClientUtils> instance = Maps.newHashMap();

    public ScpClientUtils(String ip, int port, String username, String password) {
        this.ip = ip;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    //获取工具类对象
    static synchronized public ScpClientUtils getInstance(String ip, int port, String username, String password) {
        if (instance.get(ip) == null) {
            instance.put(ip, new ScpClientUtils(ip, port, username, password));
        }
        return instance.get(ip);
    }

    /**
     * 从远程服务器下载文件到本地指定路径
     * @param remoteFile 远程文件路径
     * @param localTargetDirectory 本地目标路径
     */
    public void getFile(String remoteFile, String localTargetDirectory) {
        Connection conn = new Connection(ip, port);
        try {
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                System.err.println("authentication failed");
            }
            SCPClient client = new SCPClient(conn);
            client.get(remoteFile, localTargetDirectory);
        } catch (IOException ex) {
            Logger.getLogger(SCPClient.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            conn.close();
        }
    }

    public void putFile(String localFile, String remoteTargetDirectory) {
        putFile(localFile, null, remoteTargetDirectory);
    }

    public void putFile(String localFile, String remoteFileName, String remoteTargetDirectory) {
        putFile(localFile, remoteFileName, remoteTargetDirectory,null);
    }

    /**
     * 将本地文件推到远程服务器
     * @param localFile
     * @param remoteFileName
     * @param remoteTargetDirectory
     * @param mode
     */
    public void putFile(String localFile, String remoteFileName, String remoteTargetDirectory, String mode) {
        Connection conn = new Connection(ip, port);
        try {
            conn.connect();
            boolean isAuthenticated = conn.authenticateWithPassword(username, password);
            if (!isAuthenticated) {
                System.err.println("authentication failed");
            }
            SCPClient client = new SCPClient(conn);
            if ((mode == null) || (mode.length() == 0)) {
                mode = "0600";
            }
            if (remoteFileName == null) {
                client.put(localFile, remoteTargetDirectory);
            } else {
                client.put(localFile, remoteFileName, remoteTargetDirectory, mode);
            }
        } catch (IOException ex) {
            Logger.getLogger(ScpClientUtils.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            conn.close();
        }
    }
}
