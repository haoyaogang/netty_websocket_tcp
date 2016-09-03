/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.crystal.chat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.json.JsonObjectDecoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.crystal.chat.bean.ChannelType;
import com.crystal.chat.bean.RtmpUser;

/**
 * Handles a server-side channel.
 */
public class SecureChatServerHandler extends SimpleChannelInboundHandler<String> {

    
    public String getUserlist()
    {
        JSONArray jsonarray = new JSONArray();
        for(RtmpUser user:SocketChannelBuffer.mUserlist)
        {
            JSONObject json = new JSONObject();
            json.put("name", user.getUsername());
            json.put("rtmp",user.getRtmpurl());
            jsonarray.put(json);
        }
        return jsonarray.toString();
        
    }
    
    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        // Once session is secured, send a greeting and register the channel to the global channel
        // list so the channel received the messages from others.
       
        /*ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                new GenericFutureListener<Future<Channel>>() {
                    @Override
                    public void operationComplete(Future<Channel> future) throws Exception {
                        ctx.writeAndFlush(
                                "Welcome to " + InetAddress.getLocalHost().getHostName() + " secure chat service!\n");
                        ctx.writeAndFlush(
                                "Your session is protected by " +
                                        ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                        " cipher suite.\n");
                        
                        String rtmpurls = getUserlist();
                        System.out.println("Servier active rtmpurls="+rtmpurls);
                        ctx.writeAndFlush(rtmpurls+"\n");
                        channels.add(ctx.channel());
                    }
        });
        */
        ctx.fireChannelActive();
        /*String rtmpurls = getUserlist();
        System.out.println("Servier active rtmpurls="+rtmpurls);
        ctx.writeAndFlush(rtmpurls+"\n");
        channels.add(ctx.channel());*/
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        // Send the received message to all channels but the current one.
           System.out.println(">>>>>TCP>>>");
               processCommand(ctx.channel(),msg);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    void processCommand(Channel channel,String msg)
    {
        try
        {
            JSONObject msgobj = new JSONObject(msg);
            String action = msgobj.getString("action");
            if("Get".equals(action))
            {
                System.out.println(">>>>>TCP get>>>");
                channel.writeAndFlush(getUserlist()+"\n");
                
            }else if("Set".equals(action))
            {
                System.out.println(">>>>>TCP set listsize=>>>"+SocketChannelBuffer.tcpchannels.size());
                RtmpUser user = new RtmpUser();
                user.setUsername(msgobj.getString("name"));
                user.setRtmpurl(msgobj.getString("rtmp"));
                user.setChannelId(channel.id().asLongText());
                SocketChannelBuffer.mUserlist.add(user);
                
                for (Channel c: SocketChannelBuffer.tcpchannels) {
                    if (c != channel) {
                        System.out.println(">>>> TYPE_TCP");
                        System.out.println(">>>>>TCP other channel>>>");
                        c.writeAndFlush(msg+"\r\n");
                    } else {
                    }
                }
                for (Channel c: SocketChannelBuffer.webchannels) {
                        System.out.println(">>>>>TCP other channel>>>");
                        System.out.println(">>>> TYPE_WEBSOCKET");
                            c.writeAndFlush(new TextWebSocketFrame(msg));
                }
                
            }else if("Ping".equals(action))
            {
                //do nothing
            }
            
        }catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
