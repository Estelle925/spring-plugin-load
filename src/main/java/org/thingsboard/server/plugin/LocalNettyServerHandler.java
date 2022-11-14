package org.thingsboard.server.plugin;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LocalNettyServerHandler extends SimpleChannelInboundHandler<String> {

    /**
     * 处理读取到的msg
     *
     * @param ctx 上下文
     * @param msg 数据
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("服务端收到的消息--------" + msg);
        ctx.channel().writeAndFlush("ok");
    }

    /**
     * 断开连接
     *
     * @param ctx 傻瓜下文
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ChannelId channelId = ctx.channel().id();
        log.info("客户端id:{},断开连接,ip:{}", channelId, ctx.channel().remoteAddress());
        super.handlerRemoved(ctx);
    }

}
