package com.crystal.chat.bean;

import io.netty.channel.ChannelOption;

public class ChannelType {
    public final static int TYPE_TCP = 1;
    public final static int TYPE_WEBSOCKET = 2;
    public final static ChannelOption op = ChannelOption.newInstance("type");
}
