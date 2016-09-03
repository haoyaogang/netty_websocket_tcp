package com.crystal.chat;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;

import com.crystal.chat.bean.RtmpUser;

public class SocketChannelBuffer {
    public static final ChannelGroup tcpchannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static final ChannelGroup webchannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static final ArrayList<RtmpUser> mUserlist = new ArrayList<RtmpUser>();
}
