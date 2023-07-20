package com.example.gopalrunrunserver.net;

import com.example.gopalrunrunserver.net.tcp.ClientHandler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GSessionManager {
    private final Set<String> tcpSessionIds = new HashSet<>();
    private final Set<String> udpSessionIds = new HashSet<>();
    private int sessionSequence = 1;
    private final Map<String, ClientHandler> mapSessionId2ClientHandler = new ConcurrentHashMap<>();
    private final Map<String, Long> mapSessionId2ExpiredTime = new ConcurrentHashMap<>();

    @Scheduled(fixedDelay = 10000)
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

    public DataOutputStream getTcpOutputStreamBy(String sessionId) {
        return mapSessionId2ClientHandler.get(sessionId).getOut();
    }

    public void initUdp(String sessionId) {
        if (tcpSessionIds.contains(sessionId))
            udpSessionIds.add(sessionId);
    }

    public boolean tcpSessionIsNotValid(String sessionId) {
        return !tcpSessionIds.contains(sessionId);
    }

    public boolean udpSessionIsNotValid(String sessionId) {
        return !udpSessionIds.contains(sessionId);
    }

    public void disconnect(String sessionId) throws IOException {
        mapSessionId2ExpiredTime.remove(sessionId);
        udpSessionIds.remove(sessionId);
        tcpSessionIds.remove(sessionId);
        mapSessionId2ClientHandler.get(sessionId).close();
        mapSessionId2ClientHandler.remove(sessionId);
    }
}
