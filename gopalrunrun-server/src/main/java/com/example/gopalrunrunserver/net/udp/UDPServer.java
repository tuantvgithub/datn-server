package com.example.gopalrunrunserver.net.udp;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.game.GameManager;
import com.example.gopalrunrunserver.net.exceptions.NetworkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UDPServer {
  private DatagramSocket serverSocket;
  private final Map<String, InetAddress> mapSessionId2ClientAddress = new HashMap<>();
  private final Map<String, Integer> mapSessionId2ClientPort = new HashMap<>();

  public void start(int port) throws IOException {
    serverSocket = new DatagramSocket(port);
    while (true) {
      final byte[] buffer = new byte[1024];
      final DatagramPacket receivedPack = new DatagramPacket(buffer, buffer.length);
      serverSocket.receive(receivedPack);

      final String cs = new String(receivedPack.getData(), 0, receivedPack.getLength());
      final String[] params = cs.split(GConstant.DATA_SEPARATED);
      final UDPCommand command = UDPCommand.getByCode(params[GConstant.COMMAND_IDX]);
      if (command == null) return;

      final String sessionId = params[GConstant.SESSION_IDX];
      mapSessionId2ClientAddress.put(sessionId, receivedPack.getAddress());
      mapSessionId2ClientPort.put(sessionId, receivedPack.getPort());

      log.info("UDP-CSSC: " + cs);
      handleMessage(sessionId, cs);
    }
  }

  public void send2Client(byte[] sc, int len, String sessionId) {
    try {
      serverSocket.send(new DatagramPacket(sc, len,
          mapSessionId2ClientAddress.get(sessionId), mapSessionId2ClientPort.get(sessionId)));
    } catch (Exception ignore) {}
  }

  public void broadcastMessage(byte[] sc, int len, List<String> broadcastSessionIds) {
    broadcastSessionIds.forEach(item -> send2Client(sc, len, item));
  }

  public void handleMessage(String sessionId, String cs) throws NetworkException {
    final String sc = cs.replace(",", ".");
    final byte[] data = sc.getBytes();
    final List<String> broadcastSessionIds = GameManager.getInstance().getOtherPlayerSessionInMyRoom(sessionId);
    if (CollectionUtils.isEmpty(broadcastSessionIds)) return;
    broadcastMessage(data, data.length, broadcastSessionIds);
  }
}
