package com.example.gopalrunrunserver.net.tcp;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.models.obj.Player;
import com.example.gopalrunrunserver.models.obj.Room;
import com.example.gopalrunrunserver.net.GSessionManager;
import com.example.gopalrunrunserver.net.exceptions.NetworkException;
import com.example.gopalrunrunserver.services.RoomService;
import com.example.gopalrunrunserver.utils.GStringUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class ClientHandler {
    private final DataInputStream in;
    private final DataOutputStream out;
    private final GSessionManager gSessionManager;
    private final RoomService roomService;

    private boolean stop;

    public ClientHandler(DataInputStream in, DataOutputStream out,
                         GSessionManager gSessionManager, RoomService roomService) {
        this.in = in;
        this.out = out;
        this.gSessionManager = gSessionManager;
        this.roomService = roomService;
    }

    @SneakyThrows
    public void run() {
        while (!stop) {
            final byte[] cs = new byte[2048];
            final int byteRead = in.read(cs);
            if (byteRead <= 0) continue;
            final String msg = new String(cs, 0, byteRead, StandardCharsets.UTF_8);
            handleMessage(msg);
        }
    }

    public void close() throws IOException {
        in.close();
        out.close();
        stop = true;
    }

    private void handleMessage(String msg) throws NetworkException {
        final String[] params = msg.split(GConstant.DATA_SEPARATED);
        final TCPCommand command = TCPCommand.getByCode(params[GConstant.COMMAND_IDX]);
        if (command == null)
            throw new NetworkException("Invalid message");

        if (TCPCommand.CS_INIT_SESSION.equals(command)) {
            log.info("CSInitSession: " + msg);
            handleInitSessionCommand();
            return;
        }
        if (gSessionManager.tcpSessionIsNotValid(params[GConstant.SESSION_IDX]))
            throw new NetworkException("Invalid session");

        switch (command) {
            case CS_INIT_UDP:
                log.info("CSInitUDP: " + msg);
                handleInitUdpCommand(params);
                break;
            case CS_CREATE_ROOM:
                log.info("CSCreateRoom: " + msg);
                handleCreateRoomCommand(params);
                break;
            case CS_JOIN_ROOM:
                log.info("CSJoinRoom: " + msg);
                handleJoinRoomCommand(params);
                break;
            case CS_FIND_ROOM:
                log.info("CSFindRoom: " + msg);
                handleFindRoomCommand(params);
                break;
            case CS_START_GAME:
                log.info("CSStartGame: " + msg);
                handleStartGameCommand(params);
                break;
            case CS_END_GAME:
                log.info("CSEndGame: " + msg);
                handleEndGameCommand(params);
                break;
            case CS_DISCONNECT:
                log.info("CSDisconnect: " + msg);
                handleDisconnectCommand(params);
                break;
            case CS_OUT_ROOM:
                log.info("CSOutRoom: " + msg);
                handleOutRoomCommand(params);
                break;
            case CS_IN_SHELL:
                log.info("CSInShell: " + msg);
                handleInShellCommand(params);
                break;
            case CS_OUT_SHELL:
                log.info("CSOutShell: " + msg);
                handleOutShellCommand(params);
                break;
            case CS_DIE:
                log.info("CSDie: " + msg);
                handleDieCommand(params);
                break;
            case CS_OUT_GAME:
                log.info("CSOutGame: " + msg);
                handleOutGameCommand(params);
                break;
            case CS_UPDATE_POS_IN_ROOM:
                log.info("CSUpdatePosInRoom: " + msg);
                handleUpdatePosInRoomCommand(params);
                break;
            case CS_UPDATE_POS_IN_GAME:
                log.info("CSUpdatePosInGame: " + msg);
                handleUpdatePosInGameCommand(params);
                break;
            case CS_HIT:
                log.info("CSHit: " + msg);
                handleHitCommand(params);
                break;
            case CS_SET_GAME:
                log.info("CSSetGame: " + msg);
                handleSetGameCommand(params);
                break;
            case CS_SPAWN_PLAYER_IN_ROOM:
                log.info("CSSpawnPlayerInRoom: " + msg);
                handleSpawnPlayerInRoomCommand(params);
                break;
            case CS_SPAWN_PLAYER_AND_REQUIRE_POS_IN_ROOM:
                log.info("CSSpawnPlayerAndRequirePosInRoom: " + msg);
                handleSpawnPlayerAndRequirePosInRoomCommand(params);
                break;
            case CS_SPAWN_PLAYER_IN_GAME:
                log.info("CSSpawNPlayerInGame: " + msg);
                handleSpawnPlayerInGameCommand(params);
                break;
            case CS_GET_ROOM_INFO:
                log.info("CSGetRoomInfo: " + msg);
                handleGetRoomInfoCommand(params);
                break;
            case CS_PING:
                log.info("CSPing: " + msg);
                handlePingCommand(params);
                break;
            default:
                throw new NetworkException("Invalid message");
        }
    }

    private void send2Client(String sc, String titleLog) {
        if (GStringUtils.isNotBlank(sc)) {
            try {
                final byte[] data = sc.getBytes();
                out.write(data, 0, data.length);
                out.flush();
                log.info(titleLog + ": " + sc);
            } catch (Exception e) {
                log.error("Failed to send message: " + sc, e);
            }
        }
    }

    // cs: code;sessionId
    private void handleEndGameCommand(String[] params) {
        roomService.endGame(params[GConstant.SESSION_IDX]);
    }

    private void handlePingCommand(String[] params) {
        gSessionManager.refreshSessionTimeout(params[GConstant.SESSION_IDX]);
        log.info("SCPong");
    }

    // cs: code;sessionId;targetSessionId;pos_x;pos_y
    // sc: code;sessionId;pos_x;pos_y
    private void handleSpawnPlayerInRoomCommand(String[] params) {
        final String msg = TCPCommand.SC_SPAWN_PLAYER_IN_ROOM.getCode() + GConstant.DATA_SEPARATED +
                params[1] + GConstant.DATA_SEPARATED + params[3] + GConstant.DATA_SEPARATED + params[4];
        final DataOutputStream targetOut = gSessionManager.getTcpOutputStreamBy(params[2]);
        if (targetOut == null) return;
        final byte[] sc = msg.getBytes();
        try {
            targetOut.write(sc, 0, sc.length);
            targetOut.flush();
        } catch (Exception e) {
            throw new NetworkException(e.getMessage());
        }
        log.info("SCSpawnPlayerInRoom: " + msg);
    }

    // cs: code;sessionId;pos_x;pos_y
    // sc: code;sessionId;pos_x;pos_y
    private void handleSpawnPlayerAndRequirePosInRoomCommand(String[] params) {
        final String msg = TCPCommand.SC_SPAWN_PLAYER_AND_REQUIRE_POS_IN_ROOM.getCode() +
                GConstant.DATA_SEPARATED + params[1] + GConstant.DATA_SEPARATED +
                params[2] + GConstant.DATA_SEPARATED + params[3];
        final List<String> playerSessionIds = roomService.getAllPlayerSessionInMyRoom(params[1]).stream()
                        .filter(item -> !item.equals(params[1])).collect(Collectors.toList());
        final byte[] sc = msg.getBytes();
        playerSessionIds.forEach(item -> {
            final DataOutputStream targetOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                targetOut.write(sc, 0, sc.length);
                targetOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCSpawnPlayerAndRequirePosInRoom: " + msg);
    }

    // cs: code;sessionId;pos_x;pos_y;isSnail
    // sc: code;sessionId;pos_x;pos_y;isSnail
    private void handleSpawnPlayerInGameCommand(String[] params) {
        final String msg = TCPCommand.SC_SPAWN_PLAYER_IN_GAME.getCode() + GConstant.DATA_SEPARATED +
                params[1] + GConstant.DATA_SEPARATED + params[2] + GConstant.DATA_SEPARATED + params[3] +
                GConstant.DATA_SEPARATED + params[4];
        final List<String> playerSessionIds = roomService.getAllPlayerSessionInMyRoom(params[1]).stream()
                .filter(item -> !item.equals(params[1])).collect(Collectors.toList());
        final byte[] sc = msg.getBytes();
        playerSessionIds.forEach(item -> {
            final DataOutputStream targetOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                targetOut.write(sc, 0, sc.length);
                targetOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCSpawnPlayerInGame: " + msg);
    }

    // cs: code;sessionId
    // sc: code;time;max;chicken;isPrivate
    private void handleGetRoomInfoCommand(String[] params) {
        final Room room = roomService.getRoom(params[1]);
        if (room == null) return;
        final String msg = TCPCommand.SC_GET_ROOM_INFO.getCode() + GConstant.DATA_SEPARATED +
                room.getTimeInMinutes() + GConstant.DATA_SEPARATED + room.getMaxPlayers() +
                GConstant.DATA_SEPARATED + room.getNumOfChicken() +
                GConstant.DATA_SEPARATED + (room.isPrivate() ? 1: 0);
        final byte[] sc = msg.getBytes();
        try {
            out.write(sc, 0, sc.length);
            out.flush();
        } catch (Exception e) {
            throw new NetworkException(e.getMessage());
        }
        log.info("SCGetRoomInfo: " + msg);
    }

    /**
     * CS: code;sessionId;timeInMinutes;numOfChickens;numOfPlayers;isPrivate
     * SC(B): code;timeInMinutes;numOfChickens;numOfPlayers
     */
    private void handleSetGameCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final int time = Integer.parseInt(params[2]);
        final int chickens = Integer.parseInt(params[3]);
        final int players = Integer.parseInt(params[4]);
        final int isPrivate = Integer.parseInt(params[5]);
        final boolean success = roomService.setGame(sessionId, time, chickens, players, isPrivate);
        if (!success) return;

        final List<String> sessionIds = roomService.getAllPlayerSessionInMyRoom(sessionId);
        final String msg = TCPCommand.SC_SET_GAME.getCode() + GConstant.DATA_SEPARATED +
                time + GConstant.DATA_SEPARATED + chickens + GConstant.DATA_SEPARATED + players +
                GConstant.DATA_SEPARATED + isPrivate;
        final byte[] sc = msg.getBytes();
        sessionIds.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(sc, 0, sc.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCSetGame(B): " + msg);
    }

    /**
     * CS: code;sessionId
     * SC(B): code;sessionId
     */
    private void handleHitCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final List<String> sessionIds = roomService.getAllPlayerSessionInMyRoom(sessionId).stream()
                .filter(item -> !item.equals(sessionId))
                .collect(Collectors.toList());
        final String sc = TCPCommand.SC_HIT.getCode() + GConstant.DATA_SEPARATED + sessionId;
        final byte[] scData = sc.getBytes();
        sessionIds.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(scData, 0, scData.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCHit(B): " + sc);
    }

    /**
     * CS: code;sessionId;pos_x;pos_y
     * SC(B): code;sessionId;pos_x;pos_y
     */
    private void handleUpdatePosInRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final List<String> sessionIds = roomService.getAllPlayerSessionInMyRoom(sessionId).stream()
                .filter(item -> !item.equals(sessionId))
                .collect(Collectors.toList());
        final String sc = TCPCommand.SC_UPDATE_POS_IN_ROOM.getCode() + GConstant.DATA_SEPARATED + sessionId +
                GConstant.DATA_SEPARATED + params[2] + GConstant.DATA_SEPARATED + params[3];
        final byte[] scData = sc.getBytes();
        sessionIds.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(scData, 0, scData.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCUpdatePosInRoom(B): " + sc);
    }

    /**
     * CS: code;sessionId;pos_x;pos_y
     * SC(B): code;sessionId;pos_x;pos_y
     */
    private void handleUpdatePosInGameCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final List<String> sessionIds = roomService.getAllPlayerSessionInMyRoom(sessionId).stream()
                .filter(item -> !item.equals(sessionId))
                .collect(Collectors.toList());
        final String sc = TCPCommand.SC_UPDATE_POS_IN_GAME.getCode() + GConstant.DATA_SEPARATED + sessionId +
                GConstant.DATA_SEPARATED + params[2] + GConstant.DATA_SEPARATED + params[3];
        final byte[] scData = sc.getBytes();
        sessionIds.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(scData, 0, scData.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCUpdatePosInGame(B): " + sc);
    }

    /**
     * CS: code;sessionId
     * SC(B): code;sessionId
     */
    private void handleInShellCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final List<String> sessionIds = roomService.getAllPlayerSessionInMyRoom(sessionId).stream()
                .filter(item -> !item.equals(sessionId))
                .collect(Collectors.toList());
        final String sc = TCPCommand.SC_IN_SHELL.getCode() + GConstant.DATA_SEPARATED + sessionId;
        final byte[] scData = sc.getBytes();
        sessionIds.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(scData, 0, scData.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCInShell(B): " + sc);
    }

    /**
     * CS: code;sessionId
     * SC(B): code;sessionId
     */
    private void handleOutShellCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final List<String> sessionIds = roomService.getAllPlayerSessionInMyRoom(sessionId).stream()
                .filter(item -> !item.equals(sessionId))
                .collect(Collectors.toList());
        final String sc = TCPCommand.SC_OUT_SHELL.getCode() + GConstant.DATA_SEPARATED + sessionId;
        final byte[] scData = sc.getBytes();
        sessionIds.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(scData, 0, scData.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCOutShell(B): " + sc);
    }

    /**
     * CS: code;sessionId
     * SC(B): code;sessionId
     */
    private void handleDieCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final List<String> sessionIds = roomService.getAllPlayerSessionInMyRoom(sessionId).stream()
                .filter(item -> !item.equals(sessionId))
                .collect(Collectors.toList());
        final String sc = TCPCommand.SC_DIE.getCode() + GConstant.DATA_SEPARATED + sessionId;
        final byte[] scData = sc.getBytes();
        sessionIds.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(scData, 0, scData.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCDie(B): " + sc);
    }

    /**
     * CS: code;sessionId
     * SC(B): code;sessionId
     */
    private void handleOutGameCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final List<String> sessionIds = roomService.getAllPlayerSessionInMyRoom(sessionId).stream()
                .filter(item -> !item.equals(sessionId))
                .collect(Collectors.toList());
        roomService.outGame(sessionId);
        final String sc = TCPCommand.SC_OUT_GAME.getCode() + GConstant.DATA_SEPARATED + sessionId;
        final byte[] scData = sc.getBytes();
        sessionIds.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(scData, 0, scData.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCOutGame(B): " + sc);
    }

    /**
     * CS: code;sessionId
     * SC(B): code;sessionId;hostSessionId
     */
    private void handleOutRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final String roomCode = roomService.getRoomCode(sessionId);
        final String newHostSessionId = roomService.outRoom(sessionId);

        final List<String> players = roomService.getAllPlayerSessionInMyRoomByCode(roomCode);
        if (CollectionUtils.isEmpty(players)) return;

        final String scOutRoom = TCPCommand.SC_OUT_ROOM.getCode() + GConstant.DATA_SEPARATED + sessionId
                + GConstant.DATA_SEPARATED + newHostSessionId;
        final byte[] scOutRoomData = scOutRoom.getBytes();
        players.forEach(item -> {
            final DataOutputStream clientOut = gSessionManager.getTcpOutputStreamBy(item);
            try {
                clientOut.write(scOutRoomData, 0, scOutRoomData.length);
                clientOut.flush();
            } catch (Exception e) {
                throw new NetworkException(e.getMessage());
            }
        });
        log.info("SCOutRoom(B): " + scOutRoom);
    }

    /**
     * CS: code;sessionId
     */
    private void handleDisconnectCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        try {
            gSessionManager.disconnect(sessionId);
        } catch (IOException e) {
            throw new NetworkException(e.getMessage());
        }
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
                playerOutput.write(data, 0, data.length);
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
    private void handleFindRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final String code = roomService.joinRandomRoom(sessionId);
        final String sc = TCPCommand.SC_FIND_ROOM.getCode() + GConstant.DATA_SEPARATED + code;
        send2Client(sc, "SCFindRoom");
    }

    /**
     * CS: code;sessionId
     * SC: code;roomCode
     */
    private void handleCreateRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final String code = roomService.createNewRoom(sessionId);
        final String sc = TCPCommand.SC_CREATE_ROOM.getCode() + GConstant.DATA_SEPARATED + code;
        send2Client(sc, "SCCreateRoom");
    }

    /**
     * CS: code;sessionId;roomCode
     * SC: code;success|failed(1|0)
     */
    private void handleJoinRoomCommand(String[] params) {
        final String sessionId = params[GConstant.SESSION_IDX];
        final String roomCode = params[2];
        final int success = roomService.joinRoom(sessionId, roomCode);
        final String sc = TCPCommand.SC_JOIN_ROOM.getCode() + GConstant.DATA_SEPARATED +
                (success == 1 ? roomCode : "");
        send2Client(sc, "SCJoinRoom");
    }

    /**
     * CS: code;sessionId
     * SC: code;success|failed(1|0)
     */
    private void handleInitUdpCommand(String[] params) {
        gSessionManager.initUdp(params[GConstant.SESSION_IDX]);
        String sc = TCPCommand.SC_INIT_UDP.getCode() + GConstant.DATA_SEPARATED + 1;
        send2Client(sc, "SCInitUDP");
    }

    /**
     * CS: code
     * SC: code;sessionId
     */
    private void handleInitSessionCommand() {
        final String sessionId = gSessionManager.initSession();
        final String sc = TCPCommand.SC_INIT_SESSION.getCode() + GConstant.DATA_SEPARATED + sessionId;
        gSessionManager.registerDataOutputStream(sessionId, this);
        send2Client(sc, "SCInitSession");
    }
}
