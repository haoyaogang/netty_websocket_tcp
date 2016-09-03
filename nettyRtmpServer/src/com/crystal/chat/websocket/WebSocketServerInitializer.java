package com.crystal.chat.websocket;

import com.crystal.chat.SocketChannelBuffer;
import com.crystal.chat.bean.ChannelType;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class WebSocketServerInitializer extends ChannelInitializer<Channel>
{

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            super.handlerAdded(ctx);
            ctx.channel().config().setOption(ChannelType.op, ChannelType.TYPE_WEBSOCKET);
            SocketChannelBuffer.webchannels.add(ctx.channel());
            System.out.println(">>>>>>>WebSocketServerInitializer add an channel");
    }
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
           
    }
    @Override
    protected void initChannel(Channel Channels) throws Exception {
        
       
        
        ChannelPipeline cp = Channels.pipeline();
        cp.addLast(new HttpRequestDecoder());
        cp.addLast(new HttpResponseEncoder());
        cp.addLast(new WebSocketChannelHander());
    }
    
}