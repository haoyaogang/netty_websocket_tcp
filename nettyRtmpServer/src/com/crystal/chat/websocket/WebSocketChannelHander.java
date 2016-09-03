package com.crystal.chat.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.crystal.chat.SocketChannelBuffer;
import com.crystal.chat.bean.ChannelType;
import com.crystal.chat.bean.RtmpUser;

public class WebSocketChannelHander extends SimpleChannelInboundHandler<Object>{

    private boolean debug = true;
   
    
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        if(debug){
            System.out.println("---------------");
            System.out.println("message: "+msg.getClass());
        }
        Channel ch = ctx.channel();
        if(msg instanceof HttpRequest){
            processHttpRequest(ch, (HttpRequest)msg);
        }else if(msg instanceof WebSocketFrame){
            processWebsocketRequest(ch,(WebSocketFrame)msg);
        }else{
            //未处理的请求类型
            System.out.println("test>>>>>>>>>>.");
        }
    }
    void processHttpRequest(Channel channel,HttpRequest request){
        HttpHeaders headers = request.headers();
        if(debug){
            List<Map.Entry<String,String>> ls = headers.entries();
            for(Map.Entry<String,String> i: ls){
                System.out.println("header  "+i.getKey()+":"+i.getValue());
            }
        }       
        
        if(!HttpMethod.GET.equals(request.getMethod())
                || !"websocket".equalsIgnoreCase(headers.get("Upgrade"))){
            DefaultHttpResponse resp = new DefaultHttpResponse(
                    HttpVersion.HTTP_1_1,
                    HttpResponseStatus.BAD_REQUEST);
            channel.write(resp);            
            channel.close();
            return;
        }
        WebSocketServerHandshakerFactory wsShakerFactory = new WebSocketServerHandshakerFactory(
                "ws://"+request.headers().get(HttpHeaders.Names.HOST),
                null,false );
        WebSocketServerHandshaker wsShakerHandler = wsShakerFactory.newHandshaker(request);
        if(null==wsShakerHandler){
            //无法处理的websocket版本
            wsShakerFactory.sendUnsupportedWebSocketVersionResponse(channel);
        }else{
            wsShakerHandler.handshake(channel, request);
        }       
    }
    
    //websocket通信
    void processWebsocketRequest(Channel channel, WebSocketFrame request){      
        if(request instanceof CloseWebSocketFrame){
            SocketChannelBuffer.webchannels.remove(channel);
            
            for(RtmpUser user:SocketChannelBuffer.mUserlist)
            {
                if(user.getChannelId().equals(channel.id().asLongText()))
                {
                    SocketChannelBuffer.mUserlist.remove(user);
                    break;
                }
            }
            
            channel.close();
        }else if(request instanceof PingWebSocketFrame){            
            channel.write(new PongWebSocketFrame(request.content()));  
        }else if(request instanceof TextWebSocketFrame){
            TextWebSocketFrame txtReq = (TextWebSocketFrame) request;
            if(debug){ System.out.println("txtReq:"+txtReq.text());}
            processCommand(channel,txtReq.text());
        }else{
            //WebSocketFrame还有一些
        }
    }
    
    void processCommand(Channel channel,String msg)
    {
        try
        {
            JSONObject msgobj = new JSONObject(msg);
            String action = msgobj.getString("action");
            if("Get".equals(action))
            {
                channel.writeAndFlush(new TextWebSocketFrame(getUserlist()));
                
            }else if("Set".equals(action))
            {
                RtmpUser user = new RtmpUser();
                user.setUsername(msgobj.getString("name"));
                user.setRtmpurl(msgobj.getString("rtmp"));
                user.setChannelId(channel.id().asLongText());
                SocketChannelBuffer.mUserlist.add(user);
                
                for (Channel c: SocketChannelBuffer.webchannels) {
                    if (c != channel) {
                        System.out.println(">>>> TYPE_WEBSOCKET");
                        c.writeAndFlush(new TextWebSocketFrame(msg));
                    } else {
                    }
                }
                for (Channel c: SocketChannelBuffer.tcpchannels) {
                    System.out.println(">>>> TYPE_TCP");
                    c.writeAndFlush(msg+"\r\n");
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
}
