package com.example.gopalrunrunserver.net.tcp;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.models.obj.Player;
import com.example.gopalrunrunserver.net.GSessionManager;
import com.example.gopalrunrunserver.net.exceptions.NetworkException;
import com.example.gopalrunrunserver.services.AuthService;
import com.example.gopalrunrunserver.services.RoomService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

@AllArgsConstructor
@Slf4j
public class ClientHandler implements Runnable {
    private DataInputStream in;
    private DataOutputStream out;

    private final GSessionManager gSessionManager;
    private final AuthService authService;
    private final RoomService roomService;

    @SneakyThrows
    @Override
    public void run() {
        while (true) {
            final byte[] cs = new byte[2048];
            final int byteRead = in.read(cs);
            if (byteRead <= 0) continue;
            final String msg = new String(cs, 0, byteRead, StandardCharsets.UTF_8);
            log.info("CS: " + msg);

            final String[] params = msg.split(GConstant.DATA_SEPARATED);
            final TCPCommand command = TCPCommand.getByCode(params[GConstant.COMMAND_IDX]);
            if (command != null) {
                try {
                    final byte[] sc = handleMessage(command, params);
                    if (sc != null) {
                        out.write(sc, 0, sc.length);
                        out.flush();
                        log.info("SC: " + new String(sc, StandardCharsets.UTF_8));
                    }
                } catch (NetworkException e) {
                    log.error("Failed to handle message: " + msg, e);
                }
            }
        }
    }

    private byte[] handleMessage(TCPCommand command, String[] params) throws NetworkException {
        if (TCPCommand.CS_INIT_SESSION.equals(command))
            return handleInitSessionCommand();
        if (gSessionManager.tcpSessionIsNotValid(params[GConstant.SESSION_IDX]))
            throw new NetworkException("Invalid session");

        if (TCPCommand.CS_AUTH.equals(command))
            return handleAuthCommand(params);
        if (!authService.isAuthSession(params[GConstant.SESSION_IDX]))
            throw new NetworkException("Require auth");

        switch (command) {
            case CS_INIT_UDP:
                return handleInitUdpCommand(params);
            case CS_CREATE_ROOM:
                return handleCreateRoomCommand(params);
            case CS_JOIN_ROOM:
                return handleJoinRoomCommand(params);
            case CS_FIND_ROOM:
                return handleFindRoomCommand(params);
            case CS_START_GAME:
                handleStartGameCommand(params);
                return null;
            case CS_DISCONNECT:
                handleDisconnectCommand(params);
                return null;
            case CS_OUT_ROOM:
                handleOutRoomCommand(params);
                return null;
            default:
                log.error("Invalid message");
                throw new NetworkException("Invalid message");
        }
    }

    /**
     * CS: code;sessionId
     * SC-isHost: code
     * SC-outRoom: code;sessionId;hostSessionId
     */
    private void handleOutRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final boolean isHost = roomService.isHost(sessionId);
        final String roomCode = roomService.getRoomCode(sessionId);
        if (!roomService.outRoom(sessionId)) return;

        final List<String> players = roomService.getAllPlayerSessionInMyRoomByCode(roomCode);
        String newHostSessionId = "";
        if (isHost) {
            final String sc = TCPCommand.SC_IS_HOST.getCode();
            final byte[] data = sc.getBytes();
            newHostSessionId = players.get(0);
            roomService.setHost(newHostSessionId);
            final DataOutputStream out = gSessionManager.getTcpOutputStreamBy(newHostSessionId);
            try {
                out.write(data);
                out.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        }
        final String scOutRoom = TCPCommand.SC_OUT_ROOM.getCode() + GConstant.DATA_SEPARATED + sessionId +
                GConstant.DATA_SEPARATED + newHostSessionId;
        final byte[] scOutRoomData = scOutRoom.getBytes();
        players.forEach(item -> {
            final DataOutputStream out = gSessionManager.getTcpOutputStreamBy(item);
            try {
                out.write(scOutRoomData);
                out.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
    }

    /**
     * CS: code;sessionId
     */
    private void handleDisconnectCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        gSessionManager.disconnect(sessionId);
    }

    /**
     * CS: code;sessionId
     * SC(B): code;isChicken(1|0);pos_x;pos_y
     */
    private void handleStartGameCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final List<Player> players = roomService.startGame(sessionId);
        final String prefix = TCPCommand.SC_START_GAME.getCode() + GConstant.DATA_SEPARATED;
        players.forEach(player -> {
            final String sc = prefix + player.getIsChicken() + GConstant.DATA_SEPARATED +
                    player.getPosX() + GConstant.DATA_SEPARATED + player.getPosY();
            final byte[] data = sc.getBytes();
            final DataOutputStream playerOutput = gSessionManager.getTcpOutputStreamBy(player.getSessionId());
            try {
                playerOutput.write(data);
                playerOutput.flush();
                log.info("SCStartGame: " + sc);
            } catch (Exception e) {
                e.printStackTrace();
                throw new NetworkException(e.getMessage());
            }
        });
    }

    /**
     * CS: code;sessionId
     * SC: code;1|0
     */
    private byte[] handleFindRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final int success = roomService.joinRandomRoom(sessionId);
        final String sc = TCPCommand.SC_FIND_ROOM.getCode() + GConstant.DATA_SEPARATED + success;
        return sc.getBytes();
    }

    /**
     * CS: code;sessionId;mapId;maxPlayers;numOfChicken
     * SC: code;success|failed(1|0)
     */
    private byte[] handleCreateRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final String mapId = params[2];
        final int maxPlayers = Integer.parseInt(params[3]);
        final int numOfChicken = Integer.parseInt(params[4]);

        final int success = roomService.createNewRoom(sessionId, mapId, maxPlayers, numOfChicken);
        final String sc = TCPCommand.SC_CREATE_ROOM.getCode() + GConstant.DATA_SEPARATED + success;
        return sc.getBytes();
    }

    /**
     * CS: code;sessionId;roomCode
     * SC: code;success|failed(1|0)
     */
    private byte[] handleJoinRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final String roomCode = params[2];
        final int success = roomService.joinRoom(sessionId, roomCode);
        final String sc = TCPCommand.SC_JOIN_ROOM.getCode() + GConstant.DATA_SEPARATED + success;
        return sc.getBytes();
    }

    /**
     * CS: code;sessionId
     * SC: code;success|failed(1|0)
     */
    private byte[] handleInitUdpCommand(String[] params) {
        gSessionManager.initUdp(params[GConstant.SESSION_IDX]);
        String sc = TCPCommand.SC_INIT_UDP.getCode() + GConstant.DATA_SEPARATED + 1;
        return sc.getBytes();
    }

    /**
     * CS: code
     * SC: code;sessionId
     */
    private byte[] handleInitSessionCommand() {
        final String sessionId = gSessionManager.initSession();
        final String sc = TCPCommand.SC_INIT_SESSION.getCode() + GConstant.DATA_SEPARATED + sessionId;
        gSessionManager.registerDataOutputStream(sessionId, this.out);
        return sc.getBytes();
    }

    /**
     * CS: code;sessionId;deviceId
     * SC: code;success|failed(1|0)
     */
    private byte[] handleAuthCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final String deviceId = params[2];
        final int success = authService.auth(sessionId, deviceId);
        final String sc = TCPCommand.SC_AUTH.getCode() + GConstant.DATA_SEPARATED + success;
        return sc.getBytes();
    }
}
