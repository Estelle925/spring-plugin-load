package org.thingsboard.server.plugin;

import io.netty.channel.Channel;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author chenhaiming
 */
public class LocalTcpChannelManager {

    /**
     * 在线会话(存储注册成功的会话)
     */
    private static final ConcurrentHashMap<String, Channel> onlineChannels = new ConcurrentHashMap<>();


    /**
     * 加入
     */
    public static boolean putChannel(String mn, Channel channel) {
        if (!onlineChannels.containsKey(mn)) {
            return onlineChannels.putIfAbsent(mn, channel) == null;
        }
        return false;
    }

    /**
     * 移除
     */
    public static Channel removeChannel(String mn) {
        return onlineChannels.remove(mn);
    }

    /**
     * 获取Channel
     */
    public static Channel getChannel(String mn) {
        // 获取一个可用的会话
        Channel channel = onlineChannels.get(mn);
        if (channel != null) {
            // 连接有可能是断开，加入已经断开连接了，我们需要进行尝试重连
            if (!channel.isActive()) {
                //先移除之前的连接
                removeChannel(mn);
                return null;
            }
        }
        return channel;
    }

    /**
     * 发送消息[自定义协议]
     */
    public static <T> void sendMessage(String mn, String msg) {
        Channel channel = onlineChannels.get(mn);
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(msg);
        }
    }

    /**
     * 发送消息[自定义协议]
     */
    public static <T> void sendChannelMessage(Channel channel, String msg) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(msg);
        }
    }

    /**
     * 关闭连接
     */
    public static void closeChannel(String mn) {
        onlineChannels.get(mn).close();
    }
}
