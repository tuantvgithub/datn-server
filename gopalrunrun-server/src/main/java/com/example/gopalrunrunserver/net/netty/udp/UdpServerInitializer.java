package com.example.gopalrunrunserver.net.netty.udp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;

public class UdpServerInitializer extends ChannelInitializer<DatagramChannel> {
  @Override
  protected void initChannel(DatagramChannel datagramChannel) {
    final ChannelPipeline pipeline = datagramChannel.pipeline();
    pipeline.addLast(new UdpServerHandler());
  }
}
