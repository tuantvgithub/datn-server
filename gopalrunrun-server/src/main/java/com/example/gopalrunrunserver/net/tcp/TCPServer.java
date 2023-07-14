package com.example.gopalrunrunserver.net.tcp;

import com.example.gopalrunrunserver.net.GSessionManager;
import com.example.gopalrunrunserver.services.AuthService;
import com.example.gopalrunrunserver.services.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class TCPServer {
  private final GSessionManager gSessionManager;
  private final AuthService authService;
  private final RoomService roomService;

  public void start(int port) throws IOException {
    final ServerSocket serverSocket = new ServerSocket(port);
    while (true) {
      final Socket socket = serverSocket.accept();
      final DataInputStream in = new DataInputStream(socket.getInputStream());
      final DataOutputStream out = new DataOutputStream(socket.getOutputStream());

      final Thread clientThread = new Thread(new ClientHandler(in, out,
              gSessionManager, authService, roomService));
      clientThread.start();
    }
  }
}
