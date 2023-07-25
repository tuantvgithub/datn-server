package com.example.gopalrunrunserver.net;

import com.example.gopalrunrunserver.net.netty.tcp.NettyTcpServer;
import com.example.gopalrunrunserver.net.netty.udp.NettyUdpServer;
import com.example.gopalrunrunserver.net.tcp.TCPServer;
import com.example.gopalrunrunserver.net.udp.UDPServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class ServerRunner implements CommandLineRunner {
  private final TCPServer tcpServer;
  private final UDPServer udpServer;
//  private final NettyTcpServer nettyTcpServer;

  private final ThreadPoolTaskExecutor executor;

  private static final int TCP_PORT = 6868;
  private static final int UDP_PORT = 8686;
  private static final int NETTY_UDP_PORT = 6666;

  @Override
  public void run(String... args) throws Exception {
//    log.info("TCP server started at port " + TCP_PORT);
//    executor.execute(() -> {
//      try {
//        tcpServer.start(TCP_PORT);
//      } catch (IOException e) {
//        log.error("Failed to run TCP server");
//        throw new RuntimeException(e);
//      }
//    });

//    log.info("UDP server started at port " + UDP_PORT);
//    executor.execute(() -> {
//      try {
//        udpServer.start(UDP_PORT);
//      } catch (IOException e) {
//        log.error("Failed to run UDP server");
//        throw new RuntimeException(e);
//      }
//    });

    executor.execute(() -> NettyTcpServer.getInstance().start(TCP_PORT));
    executor.execute(() -> NettyUdpServer.getInstance().start(UDP_PORT));
  }
}
