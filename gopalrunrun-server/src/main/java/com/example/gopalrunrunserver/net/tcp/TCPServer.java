package com.example.gopalrunrunserver.net.tcp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@Component
@RequiredArgsConstructor
@Slf4j
public class TCPServer {
  private final ThreadPoolTaskExecutor taskExecutor;

  public void start(int port) throws IOException {
    final ServerSocket serverSocket = new ServerSocket(port);
    while (true) {
      final Socket socket = serverSocket.accept();
      final DataInputStream in = new DataInputStream(socket.getInputStream());
      final DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      final ClientHandler clientHandler = new ClientHandler(in, out);
      taskExecutor.execute(clientHandler::run);
    }
  }
}
