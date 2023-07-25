package com.example.gopalrunrunserver.net;

import com.example.gopalrunrunserver.game.GameManager;
import com.example.gopalrunrunserver.net.tcp.ClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class GSessionManager {
  private final Set<String> tcpSessionIds = new HashSet<>();
  private int sessionSequence = 1;
  private final Map<String, ClientHandler> mapSessionId2ClientHandler = new ConcurrentHashMap<>();
  private final Map<String, Long> mapSessionId2ExpiredTime = new ConcurrentHashMap<>();

  private static GSessionManager instance;

  private GSessionManager() {
  }

  public static GSessionManager getInstance() {
    if (instance == null) instance = new GSessionManager();
    return instance;
  }

//  @Scheduled(fixedDelay = 10000)
  private void killTimeoutSession() throws IOException {
    final long current = System.currentTimeMillis();
    for (Map.Entry<String, Long> entry : mapSessionId2ExpiredTime.entrySet()) {
      if (current >= entry.getValue()) {
        disconnect(entry.getKey());
      }
    }
  }

  public void refreshSessionTimeout(String sessionId) {
    mapSessionId2ExpiredTime.put(sessionId, System.currentTimeMillis() + 10000L);
  }

  public String initSession() {
    final String sessionId = String.valueOf(sessionSequence);
    sessionSequence += 1;
    tcpSessionIds.add(sessionId);
    mapSessionId2ExpiredTime.put(sessionId, System.currentTimeMillis() + 10000L);

    return sessionId;
  }

  public void registerDataOutputStream(String sessionId, ClientHandler clientHandler) {
    mapSessionId2ClientHandler.put(sessionId, clientHandler);
  }

  public void sendTCPMessage(String sc, String sessionId) {
    if (!mapSessionId2ClientHandler.containsKey(sessionId)) return;
    mapSessionId2ClientHandler.get(sessionId).send2Client(sc);
  }

  public void sendTCPMessage(byte[] sc, int len, String sessionId) {
    if (!mapSessionId2ClientHandler.containsKey(sessionId)) return;
    mapSessionId2ClientHandler.get(sessionId).send2Client(sc, len);
  }

  public boolean tcpSessionIsNotValid(String sessionId) {
    return !tcpSessionIds.contains(sessionId);
  }

  public void disconnect(String sessionId) throws IOException {
    log.info("Disconnect session: " + sessionId);
    mapSessionId2ExpiredTime.remove(sessionId);
    tcpSessionIds.remove(sessionId);
    mapSessionId2ClientHandler.get(sessionId).close();
    mapSessionId2ClientHandler.remove(sessionId);
    GameManager.getInstance().outRoom(sessionId);
    GameManager.getInstance().outGame(sessionId);
  }
}
