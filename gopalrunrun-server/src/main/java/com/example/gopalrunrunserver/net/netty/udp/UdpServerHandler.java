package com.example.gopalrunrunserver.net.netty.udp;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.game.GameManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UdpServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket packet) {
    try {
      final String msg = packet.content().toString(CharsetUtil.UTF_8);
      log.info(msg);
      final String[] params = msg.split(GConstant.DATA_SEPARATED);
      final String sessionId = params[0];
      if (params.length == 1) {
        NettyUdpServer.getInstance().addClient(sessionId, packet.sender());
        return;
      }
      NettyUdpServer.getInstance().broadcast(ctx, packet,
          GameManager.getInstance().getAllPlayerSessionInMyRoom(sessionId));
    } catch (Exception ignore) {
    }
  }
}
