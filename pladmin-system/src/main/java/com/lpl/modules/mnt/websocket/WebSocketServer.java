package com.lpl.modules.mnt.websocket;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author lpl
 * webSocket服务端
 */
@ServerEndpoint("/webSocket/{sid}")
@Slf4j
@Component
public class WebSocketServer {

    /**
     * concurrent包的线程安全Set，用于存放每个客户端对应的WebSocket对象
     */
    private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<>();

    /**
     * 与某个客户端的连接会话，通过它来给客户端发送数据
     */
    private Session session;

    /**
     * 接收到的sid
     */
    private String sid = "";

    /**
     * 建立连接成功时调用的方法，加入当前WebSocketServer
     * @param session 会话对象
     * @param sid 接收的参数
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        this.session = session;
        for (WebSocketServer webSocketServer : webSocketSet) {
            //如果已经存在就删除，防止重复推送消息
            if (webSocketServer.sid.equals(sid)) {
                webSocketSet.remove(webSocketServer);
            }
        }
        webSocketSet.add(this);
        this.sid = sid;
    }

    /**
     * 收到客户端消息后调用的方法
     * @param message 客户端发来的消息
     * @param session 会话对象
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来"+sid+"的信息:"+message);
        //群发消息
        for (WebSocketServer webSocketServer : webSocketSet) {
            try {
                webSocketServer.sendMessage(message);
            } catch (IOException e) {
                log.error(e.getMessage(),e);
            }
        }
    }
    /**
     * 实现服务器主动推送
     */
    private void sendMessage(String message) throws IOException {
        this.session.getBasicRemote().sendText(message);
    }

    /**
     * 发生错误时执行的方法
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
    }

    /**
     * 连接关闭时执行的方法，移除当前WebSocketServer
     */
    @OnClose
    public void onClose(){
        webSocketSet.remove(this);
    }

    /**
     * 群发自定义消息
     * @param socketMsg
     * @param sid
     * @throws IOException
     */
    public static void sendInfo(SocketMsg socketMsg, @PathParam("sid") String sid) throws IOException {
        String message = JSONObject.toJSONString(socketMsg);
        log.info("推送消息到"+sid+"，推送内容:"+message);
        for (WebSocketServer webSocketServer : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if(sid==null) {
                    webSocketServer.sendMessage(message);
                }else if(webSocketServer.sid.equals(sid)){
                    webSocketServer.sendMessage(message);
                }
            } catch (IOException ignored) { }
        }
    }
}
