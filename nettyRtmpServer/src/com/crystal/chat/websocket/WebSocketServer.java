package com.crystal.chat.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

import com.crystal.chat.Port;
import com.crystal.chat.SecureChatServerInitializer;
import com.crystal.chat.bean.ChannelType;

public class WebSocketServer {

    public static void main(String[] args) {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
           
             .channel(NioServerSocketChannel.class)
             .option(ChannelType.op, ChannelType.TYPE_WEBSOCKET)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new WebSocketServerInitializer());
//             .childHandler(new SecureChatServerInitializer(null));
           ChannelFuture future =  b.bind("127.0.0.1",Port.WEBSOCKET_PORT).sync().channel().closeFuture().sync();
           
        }catch(Exception ex){
            ex.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    
    
}
