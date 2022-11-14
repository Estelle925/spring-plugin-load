package org.thingsboard.server.plugin;

import cn.hutool.core.thread.ThreadUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * @author chenhaiming
 */
@Slf4j
public class LocalNettyServer {

    private static final int DEFAULT_PORT = 60987;
    /**
     * bossGroup只是处理连接请求
     */
    private static EventLoopGroup bossGroup = null;
    /**
     * workGroup处理非连接请求，如果牵扯到数据量处理业务非常耗时的可以再单独新建一个eventLoopGroup,
     * 并在childHandler初始化的时候添加到pipeline绑定
     */
    private static EventLoopGroup workGroup = null;
    private Channel serverChannel;

    /**
     * 启动localNetty服务
     *
     * @return 启动结果
     */
    public boolean start() {
        bossGroup = new NioEventLoopGroup();
        workGroup = new NioEventLoopGroup();
        //创建服务端启动对象
        ServerBootstrap bootstrap = new ServerBootstrap();
        try {
            //使用链式编程来设置
            //设置两个线程组
            bootstrap.group(bossGroup, workGroup)
                    //使用NioSocketChannel作为服务器的通道实现
                    .channel(NioServerSocketChannel.class)
                    //设置线程队列得到的连接数
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    //设置保持活动连接状态
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    //设置处理器  WorkerGroup 的 EvenLoop 对应的管道设置处理器
                    .childHandler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel ch) {
                            log.info("--------------有客户端连接");
                            ch.pipeline().addLast(new StringDecoder());
                            ch.pipeline().addLast(new StringEncoder());
                            ch.pipeline().addLast(new LocalNettyServerHandler());
                        }
                    });
            //绑定端口, 同步等待成功;
            ChannelFuture future = bootstrap.bind(DEFAULT_PORT).sync();
            log.info("localNetty服务启动成功，ip：{}，端口：{}", InetAddress.getLocalHost().getHostAddress(), DEFAULT_PORT);
            serverChannel = future.channel();
            ThreadUtil.execute(() -> {
                //等待服务端监听端口关闭
                try {
                    future.channel().closeFuture().sync();
                    log.info("localNetty服务正常关闭成功，ip：{}，端口：{}", InetAddress.getLocalHost().getHostAddress(), DEFAULT_PORT);
                } catch (InterruptedException | UnknownHostException e) {
                    e.printStackTrace();
                } finally {
                    shutdown();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            log.error("localNetty服务异常，异常原因：{}", e.getMessage());
            return false;
        }
        return true;
    }


    /**
     * 关闭当前server
     */
    public boolean close() {
        log.error("localNetty服务异常正在被关闭");
        if (serverChannel != null) {
            serverChannel.close();//关闭服务
            try {
                //保险起见
                serverChannel.closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } finally {
                shutdown();
                serverChannel = null;
            }
        }
        return true;
    }

    /**
     * 优雅关闭
     */
    private void shutdown() {
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }


}