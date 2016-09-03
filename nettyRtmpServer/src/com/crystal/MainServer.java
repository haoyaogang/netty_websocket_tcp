package com.crystal;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import com.crystal.chat.Port;
import com.crystal.chat.SecureChatServerInitializer;
import com.crystal.chat.bean.ChannelType;
import com.crystal.chat.websocket.WebSocketServerInitializer;

public class MainServer {

    public static void main(String[] args) {
        MainServer server = new MainServer();
        server.runWeb();
        server.runTcp();
    }
    
    public static void runTcp()
    {
        new Thread(
                new Runnable() {
                    
                    @Override
                    public void run() {
                        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                        EventLoopGroup workerGroup = new NioEventLoopGroup();
                        try {
                            ServerBootstrap b = new ServerBootstrap();
                            b.group(bossGroup, workerGroup)
                           
                             .channel(NioServerSocketChannel.class)
                             .option(ChannelType.op, ChannelType.TYPE_TCP)
                             .handler(new LoggingHandler(LogLevel.INFO))
                             .childHandler(new SecureChatServerInitializer(null));

                           ChannelFuture future =  b.bind("127.0.0.1",Port.PORT).sync().channel().closeFuture().sync();
                           
                        }catch(Exception ex){
                            ex.printStackTrace();
                        } finally {
                            bossGroup.shutdownGracefully();
                            workerGroup.shutdownGracefully();
                        }
                    }
                }
                ).start();
    }
    public void runWeb()
    {
        new Thread(
                new Runnable() {
                    
                    @Override
                    public void run() {
                        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                        EventLoopGroup workerGroup = new NioEventLoopGroup();
                        try {
                            ServerBootstrap b = new ServerBootstrap();
                            b.group(bossGroup, workerGroup)
                           
                             .channel(NioServerSocketChannel.class)
                             .option(ChannelType.op, ChannelType.TYPE_WEBSOCKET)
                             .handler(new LoggingHandler(LogLevel.INFO))
                             .childHandler(new WebSocketServerInitializer());
//                             .childHandler(new SecureChatServerInitializer(null));
                           ChannelFuture future =  b.bind("127.0.0.1",Port.WEBSOCKET_PORT).sync().channel().closeFuture().sync();
                           
                        }catch(Exception ex){
                            ex.printStackTrace();
                        } finally {
                            bossGroup.shutdownGracefully();
                            workerGroup.shutdownGracefully();
                        }
                    }
                }
                ).start();
    }
}
