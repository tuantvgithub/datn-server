package com.example.gopalrunrunserver.net.udp;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.net.GSessionManager;
import com.example.gopalrunrunserver.net.exceptions.NetworkException;
import com.example.gopalrunrunserver.services.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class UDPServer {
    private final GSessionManager gSessionManager;
    private final RoomService roomService;
    private DatagramSocket serverSocket;

    private final Map<String, InetAddress> mapSessionId2ClientAddress = new HashMap<>();
    private final Map<String, Integer> mapSessionId2ClientPort = new HashMap<>();

    public void start(int port) throws IOException {
        serverSocket = new DatagramSocket(port);
        while (true) {
            final byte[] buffer = new byte[2048];
            final DatagramPacket receivedPack = new DatagramPacket(buffer, buffer.length);
            serverSocket.receive(receivedPack);

            final String cs = new String(receivedPack.getData(), 0, receivedPack.getLength());
            final String[] params = cs.split(GConstant.DATA_SEPARATED);
            final String sessionId = params[GConstant.SESSION_IDX];
            if (gSessionManager.udpSessionIsNotValid(sessionId)) continue;

            mapSessionId2ClientAddress.put(sessionId, receivedPack.getAddress());
            mapSessionId2ClientPort.put(sessionId, receivedPack.getPort());

            final UDPCommand command = UDPCommand.getByCode(params[GConstant.COMMAND_IDX]);
            if (command != null) {
                try {
                    handleMessage(command, params);
                } catch (Exception e) {
                    log.error("Failed to handle udp message: " + cs, e);
                }
            }
        }
    }

    public void handleMessage(UDPCommand command, String[] params) throws NetworkException {
        switch (command) {
            case CS_SEND_POS_IN_ROOM:
                handleCSSendPos(params);
                break;
            case CS_BROADCAST_POS_IN_ROOM:
                handleCSBroadcastPos(params);
                break;
            case CS_BROADCAST_MOVE_IN_ROOM:
                handleCSBroadcastMove(params);
                break;
            case CS_SEND_POS_IN_GAME:
                handleCSSendPosInGame(params);
                break;
            case CS_BROADCAST_POS_IN_GAME:
                handleCSBroadcastPosInGame(params);
                break;
            case CS_BROADCAST_MOVE_IN_GAME:
                handleCSBroadcastMoveInGame(params);
                break;
            case CS_UPDATE_POS_IN_GAME:
                handleCSUpdatePosInGame(params);
                break;
            case CS_UPDATE_POS_IN_ROOM:
                handleCSUpdatePosInRoom(params);
                break;
            default:
                throw new NetworkException("Invalid message");
        }
    }

    // cs: code;sessionId;pos_x;pos_y
    // sc: code;sessionId;pos_x;pos_y
    private void handleCSUpdatePosInGame(String[] params) {
        final String excludeSessionId = params[GConstant.SESSION_IDX];
        final Set<String> broadcastSessionIds = roomService.getAllPlayerSessionInMyRoom(excludeSessionId)
                .stream().filter(item -> !item.equals(excludeSessionId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(broadcastSessionIds)) return;
        final String msg = UDPCommand.SC_UPDATE_POS_IN_GAME.getCode() + GConstant.DATA_SEPARATED +
                excludeSessionId + GConstant.DATA_SEPARATED + params[2] + GConstant.DATA_SEPARATED + params[3];
        final byte[] sc = msg.getBytes();
        broadcastSessionIds.forEach(sessionId -> {
            final DatagramPacket resPacket = new DatagramPacket(sc, sc.length,
                    mapSessionId2ClientAddress.get(sessionId), mapSessionId2ClientPort.get(sessionId));
            try {
                serverSocket.send(resPacket);
            } catch (IOException e) {
                throw new NetworkException(e.getMessage());
            }
        });
    }

    private void handleCSUpdatePosInRoom(String[] params) {
        final String excludeSessionId = params[GConstant.SESSION_IDX];
        final Set<String> broadcastSessionIds = roomService.getAllPlayerSessionInMyRoom(excludeSessionId)
                .stream().filter(item -> !item.equals(excludeSessionId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(broadcastSessionIds)) return;
        final String msg = UDPCommand.SC_UPDATE_POS_IN_ROOM.getCode() + GConstant.DATA_SEPARATED +
                excludeSessionId + GConstant.DATA_SEPARATED + params[2] + GConstant.DATA_SEPARATED + params[3];
        final byte[] sc = msg.getBytes();
        broadcastSessionIds.forEach(sessionId -> {
            final DatagramPacket resPacket = new DatagramPacket(sc, sc.length,
                    mapSessionId2ClientAddress.get(sessionId), mapSessionId2ClientPort.get(sessionId));
            try {
                serverSocket.send(resPacket);
            } catch (IOException e) {
                throw new NetworkException(e.getMessage());
            }
        });
    }

    /**
     * CS: code;sessionId;targetSessionId;pos_x;pos_y;isSnail(1|0)
     * SC: code;sessionId;pos_x;pos_y;isSnail
     */
    private void handleCSSendPosInGame(String[] params) {
        final String sc = UDPCommand.SC_SEND_POS_IN_GAME.getCode() + GConstant.DATA_SEPARATED +
                params[GConstant.SESSION_IDX] + GConstant.DATA_SEPARATED + params[3] +
                GConstant.DATA_SEPARATED + params[4] + GConstant.DATA_SEPARATED + params[5];
        final byte[] data = sc.getBytes();
        final DatagramPacket packet = new DatagramPacket(data, data.length,
                mapSessionId2ClientAddress.get(params[2]), mapSessionId2ClientPort.get(params[2]));
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * CS: code;sessionId;pos_x;pos_y;isSnail(1|0)
     * SC: code;sessionId;pos_x;pos_y;isSnail
     */
    private void handleCSBroadcastPosInGame(String[] params) {
        final String excludeSessionId = params[GConstant.SESSION_IDX];
        final Set<String> broadcastSessionIds = roomService.getAllPlayerSessionInMyRoom(excludeSessionId).stream()
                .filter(item -> !item.equals(excludeSessionId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(broadcastSessionIds)) return;
        final String sc = UDPCommand.SC_BROADCAST_POS_IN_GAME.getCode() + GConstant.DATA_SEPARATED +
                excludeSessionId + GConstant.DATA_SEPARATED + params[2] +
                GConstant.DATA_SEPARATED + params[3] + GConstant.DATA_SEPARATED + params[4];
        broadcastSessionIds.forEach(sessionId -> {
            byte[] resBuffer = sc.getBytes();
            final DatagramPacket resPacket = new DatagramPacket(resBuffer, resBuffer.length,
                    mapSessionId2ClientAddress.get(sessionId), mapSessionId2ClientPort.get(sessionId));
            try {
                serverSocket.send(resPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * CS: code;sessionId;move_x;move_y
     * SC: code;sessionId;move_x;move_y
     */
    private void handleCSBroadcastMoveInGame(String[] params) {
        final String excludeSessionId = params[GConstant.SESSION_IDX];
        final Set<String> broadcastSessionIds = roomService.getAllPlayerSessionInMyRoom(excludeSessionId).stream()
                .filter(item -> !item.equals(excludeSessionId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(broadcastSessionIds)) return;
        final String sc = UDPCommand.SC_BROADCAST_MOVE_IN_GAME.getCode() + GConstant.DATA_SEPARATED +
                excludeSessionId + GConstant.DATA_SEPARATED + params[2] + GConstant.DATA_SEPARATED + params[3];
        broadcastSessionIds.stream().filter(mapSessionId2ClientAddress::containsKey)
                .forEach(sessionId -> {
                    byte[] resBuffer = sc.getBytes();
                    final DatagramPacket resPacket = new DatagramPacket(resBuffer, resBuffer.length,
                            mapSessionId2ClientAddress.get(sessionId), mapSessionId2ClientPort.get(sessionId));
                    try {
                        serverSocket.send(resPacket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * CS: code;sessionId;targetSessionId;pos_x;pos_y
     * SC: code;sessionId;pos_x;pos_y
     */
    private void handleCSSendPos(String[] params) {
        final String sc = UDPCommand.SC_SEND_POS_IN_ROOM.getCode() + GConstant.DATA_SEPARATED + params[GConstant.SESSION_IDX] +
                GConstant.DATA_SEPARATED + params[3] + GConstant.DATA_SEPARATED + params[4];
        final byte[] data = sc.getBytes();
        final DatagramPacket packet = new DatagramPacket(data, data.length,
                mapSessionId2ClientAddress.get(params[2]), mapSessionId2ClientPort.get(params[2]));
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * CS: code;sessionId;pos_x;pos_y
     * SC: code;sessionId;pos_x;pos_y
     */
    private void handleCSBroadcastPos(String[] params) {
        final String excludeSessionId = params[GConstant.SESSION_IDX];
        final Set<String> broadcastSessionIds = roomService.getAllPlayerSessionInMyRoom(excludeSessionId).stream()
                .filter(item -> !item.equals(excludeSessionId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(broadcastSessionIds)) return;
        final String sc = UDPCommand.SC_BROADCAST_POS_IN_ROOM.getCode() + GConstant.DATA_SEPARATED +
                excludeSessionId + GConstant.DATA_SEPARATED + params[2] + GConstant.DATA_SEPARATED + params[3];
        broadcastSessionIds.forEach(sessionId -> {
            byte[] resBuffer = sc.getBytes();
            final DatagramPacket resPacket = new DatagramPacket(resBuffer, resBuffer.length,
                    mapSessionId2ClientAddress.get(sessionId), mapSessionId2ClientPort.get(sessionId));
            try {
                serverSocket.send(resPacket);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * CS: code;sessionId;move_x;move_y
     * SC: code;sessionId;move_x;move_y
     */
    private void handleCSBroadcastMove(String[] params) {
        final String excludeSessionId = params[GConstant.SESSION_IDX];
        final Set<String> broadcastSessionIds = roomService.getAllPlayerSessionInMyRoom(excludeSessionId).stream()
                .filter(item -> !item.equals(excludeSessionId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isEmpty(broadcastSessionIds)) return;
        final String sc = UDPCommand.SC_BROADCAST_MOVE_IN_ROOM.getCode() + GConstant.DATA_SEPARATED +
                excludeSessionId + GConstant.DATA_SEPARATED + params[2] + GConstant.DATA_SEPARATED + params[3];
        broadcastSessionIds.stream().filter(mapSessionId2ClientAddress::containsKey)
                .forEach(sessionId -> {
                    byte[] resBuffer = sc.getBytes();
                    final DatagramPacket resPacket = new DatagramPacket(resBuffer, resBuffer.length,
                            mapSessionId2ClientAddress.get(sessionId), mapSessionId2ClientPort.get(sessionId));
                    try {
                        serverSocket.send(resPacket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }
}
