package com.lpl.modules.mnt.websocket;

/**
 * @author lpl
 * WebSocket消息类型枚举
 */
public enum MsgType {

    /** 连接 */
    CONNECT,
    /** 关闭 */
    CLOSE,
    /** 信息 */
    INFO,
    /** 错误 */
    ERROR
}
