package com.example.gopalrunrunserver.net.netty.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class NettyUdpServer {
  private static NettyUdpServer instance;
  private final Map<String, InetSocketAddress> mapSessionId2ClientAddress = new ConcurrentHashMap<>();

  private NettyUdpServer() {

  }

  public static NettyUdpServer getInstance() {
    if (instance == null) instance = new NettyUdpServer();
    return instance;
  }

  public void addClient(String sessionId, InetSocketAddress address) {
    mapSessionId2ClientAddress.put(sessionId, address);
  }

  public void removeClient(String sessionId) {
    mapSessionId2ClientAddress.remove(sessionId);
  }

  public void broadcast(ChannelHandlerContext ctx, DatagramPacket packet, List<String> sessionIds) {
    sessionIds.parallelStream().forEach(sessionId -> {
      final DatagramPacket outgoingPacket =
              new DatagramPacket(packet.content().retain(), mapSessionId2ClientAddress.get(sessionId));
      ctx.writeAndFlush(outgoingPacket);
    });
  }

  public void start(int port) {
    final NioEventLoopGroup group = new NioEventLoopGroup();
    try {
      final Bootstrap bootstrap = new Bootstrap();
      bootstrap.group(group)
          .channel(NioDatagramChannel.class)
          .option(ChannelOption.SO_BROADCAST, true)
          .handler(new UdpServerInitializer());

      final ChannelFuture channelFuture = bootstrap.bind(port).sync();
      log.info("Netty udp server is listening on port " + port);

      channelFuture.channel().closeFuture().await();
    } catch (InterruptedException ignore) {

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      group.shutdownGracefully();
    }
  }
}
