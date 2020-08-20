package com.lpl.modules.mnt.websocket;

import lombok.Data;

/**
 * @author lpl
 * WebSocket消息对象
 */
@Data
public class SocketMsg {

    private String msg;

    private MsgType msgType;

    public SocketMsg(String msg, MsgType msgType) {
        this.msg = msg;
        this.msgType = msgType;
    }
}
