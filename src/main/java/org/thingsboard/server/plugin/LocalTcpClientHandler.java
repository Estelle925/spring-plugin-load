package org.thingsboard.server.plugin;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author chenhaiming
 */
public class LocalTcpClientHandler extends SimpleChannelInboundHandler<String> {
    Logger logger = LoggerFactory.getLogger(LocalTcpClientHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) {
        logger.warn("接收消息：" + s);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.warn(".......................tcp断开连接.........................");
        //移除
        Channel channel = ctx.channel();
        channel.close().sync();
        super.channelInactive(ctx);
    }

}
