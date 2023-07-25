package com.example.gopalrunrunserver.net.netty.tcp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class NettyTcpServer {
  private static NettyTcpServer instance;
  private final Map<String, Channel> mapSessionId2ClientChanel = new ConcurrentHashMap<>();

  private NettyTcpServer() {

  }

  public static NettyTcpServer getInstance() {
    if (instance == null) instance = new NettyTcpServer();
    return instance;
  }

  public boolean isValidSession(String session) {
    return mapSessionId2ClientChanel.containsKey(session);
  }

  public boolean sessionExists(String session) {
    return mapSessionId2ClientChanel.containsKey(session);
  }

  public void addClient(String session, Channel channel) {
    mapSessionId2ClientChanel.put(session, channel);
  }

  public void removeClient(String session) {
    mapSessionId2ClientChanel.remove(session);
  }

  public void broadcast(String msg, List<String> sessionIds) {
    final ByteBuf byteBuf = Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8);
    sessionIds.parallelStream().forEach(sessionId ->
        mapSessionId2ClientChanel.get(sessionId).writeAndFlush(byteBuf));
  }

  public void sendMessage2Client(String msg, String session) {
    mapSessionId2ClientChanel.get(session).writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
  }

  public void start(int port) {
    final EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    final EventLoopGroup workerGroup = new NioEventLoopGroup();
    try {
      final ServerBootstrap bootstrap = new ServerBootstrap();
      bootstrap
          .group(bossGroup, workerGroup)
          .channel(NioServerSocketChannel.class)
          .localAddress(new InetSocketAddress(port))
          .childHandler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
              channel.pipeline().addLast(new TcpServerHandler());
            }
          });

      final ChannelFuture future = bootstrap.bind().sync();
      log.info("Netty tcp server is listening on port " + port);

      future.channel().closeFuture().sync();
    } catch (Exception ignore) {

    } finally {
      bossGroup.shutdownGracefully();
      workerGroup.shutdownGracefully();
    }
  }
}
