package com.example.gopalrunrunserver.net;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.utils.GStringUtils;
import org.springframework.stereotype.Component;

import java.io.DataOutputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GSessionManager {
    private final Set<String> tcpSessionIds = new HashSet<>();
    private final Set<String> udpSessionIds = new HashSet<>();
    private final Map<String, DataOutputStream> mapSessionId2Out = new ConcurrentHashMap<>();

    public String initSession() {
        final String sessionId = GStringUtils.random(GConstant.SESSION_ID_LENGTH);
        if (tcpSessionIds.contains(sessionId))
            return initSession();
        tcpSessionIds.add(sessionId);
        return sessionId;
    }

    public void registerDataOutputStream(String sessionId, DataOutputStream out) {
        mapSessionId2Out.put(sessionId, out);
    }

    public DataOutputStream getTcpOutputStreamBy(String sessionId) {
        return mapSessionId2Out.get(sessionId);
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

    public void disconnect(String sessionId) {
        tcpSessionIds.remove(sessionId);
        udpSessionIds.remove(sessionId);
    }
}
