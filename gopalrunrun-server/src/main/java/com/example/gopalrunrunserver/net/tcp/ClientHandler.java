package com.example.gopalrunrunserver.net.tcp;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.game.GameManager;
import com.example.gopalrunrunserver.models.obj.Player;
import com.example.gopalrunrunserver.models.obj.Room;
import com.example.gopalrunrunserver.net.GSessionManager;
import com.example.gopalrunrunserver.net.exceptions.NetworkException;
import com.example.gopalrunrunserver.net.netty.udp.NettyUdpServer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

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

  private boolean stop;

  public ClientHandler(DataInputStream in, DataOutputStream out) {
    this.in = in;
    this.out = out;
  }

  @SneakyThrows
  public void run() {
    while (!stop) {
      final byte[] cs = new byte[1024];
      final int byteRead = in.read(cs);
      if (byteRead > 0) {
        handleMessage(cs, byteRead);
      }
    }
  }

  public void close() throws IOException {
    in.close();
    out.close();
    stop = true;
  }

  private void handleMessage(byte[] cs, int byteRead) throws NetworkException {
    final String msg = new String(cs, 0, byteRead, StandardCharsets.UTF_8);
    final String[] params = msg.split(GConstant.DATA_SEPARATED);
    final TCPCommand command = TCPCommand.getByCode(params[GConstant.COMMAND_IDX]);
    if (command == null) return;

    if (TCPCommand.INIT_SESSION.equals(command)) {
      log.info("CSInitSession: {}", msg);
      handleInitSessionCommand();
      return;
    }
    final String sessionId = params[GConstant.SESSION_IDX];
    if (GSessionManager.getInstance().tcpSessionIsNotValid(sessionId))
      throw new NetworkException("Invalid session");

    switch (command) {
      case PING:
        log.info("CSPing: " + msg);
        handlePingCommand(params);
        break;
      case DISCONNECT:
        log.info("CSDisconnect: " + msg);
        handleDisconnectCommand(params);
        break;
      case CREATE_ROOM:
        log.info("CSCreateRoom: " + msg);
        handleCreateRoomCommand(params);
        break;
      case JOIN_ROOM:
        log.info("CSJoinRoom: " + msg);
        handleJoinRoomCommand(params);
        break;
      case FIND_ROOM:
        log.info("CSFindRoom: " + msg);
        handleFindRoomCommand(params);
        break;
      case START_GAME:
        log.info("CSStartGame: " + msg);
        handleStartGameCommand(params);
        break;
      case END_GAME:
        log.info("CSEndGame: " + msg);
        handleEndGameCommand(params);
        break;
      case OUT_ROOM:
        log.info("CSOutRoom: " + msg);
        handleOutRoomCommand(params);
        break;
      case IN_SHELL:
        log.info("CSInShell: " + msg);
        handleInShellCommand(cs, byteRead, sessionId);
        break;
      case OUT_SHELL:
        log.info("CSOutShell: " + msg);
        handleOutShellCommand(cs, byteRead, sessionId);
        break;
      case OUT_GAME:
        log.info("CSOutGame: " + msg);
        handleOutGameCommand(cs, byteRead, sessionId);
        break;
      case SET_GAME:
        log.info("CSSetGame: " + msg);
        handleSetGameCommand(cs, byteRead, params);
        break;
      case GET_ROOM_INFO:
        log.info("CSGetRoomInfo: " + msg);
        handleGetRoomInfoCommand(params);
        break;
      case SPAWN_PLAYER_IN_ROOM:
        log.info("CSSpawnPlayerInRoom: " + msg);
        handleSpawnPlayerInRoomCommand(params);
        break;
      case SPAWN_PLAYER_AND_REQUIRE_POS_IN_ROOM:
        log.info("CSSpawnPlayerAndRequirePosInRoom: " + msg);
        handleSpawnPlayerAndRequirePosInRoomCommand(params);
        break;
      case SPAWN_PLAYER_IN_GAME:
        log.info("CSSpawnPlayerInGame: " + msg);
        handleSpawnPlayerInGameCommand(params);
        break;
      case INIT_UDP:
        log.info("CSInitUdp: " + msg);
        handleInitUdpCommand(sessionId);
        break;
      case CLOSE_UDP:
        log.info("CSCloseUdp: " + msg);
        handleCloseUdpCommand(sessionId);
        break;
      default:
        throw new NetworkException("Invalid message");
    }
  }

  private void handleInitUdpCommand(String sessionId) {

  }

  private void handleCloseUdpCommand(String sessionId) {

  }

  public void send2Client(String sc) {
    try {
      final byte[] data = sc.getBytes();
      out.write(data, 0, data.length);
      out.flush();
    } catch (Exception e) {
      log.error("Failed to send message: " + sc, e);
    }
  }

  public void send2Client(byte[] sc, int len) {
    try {
      out.write(sc, 0, len);
      out.flush();
    } catch (Exception e) {
      log.error("Failed to send message in byte[]: ", e);
    }
  }

  private void handleEndGameCommand(String[] params) {
    GameManager.getInstance().endGame(params[GConstant.SESSION_IDX]);
  }

  private void handlePingCommand(String[] params) {
    GSessionManager.getInstance().refreshSessionTimeout(params[GConstant.SESSION_IDX]);
    log.info("SCPong");
  }

  private void handleSpawnPlayerInRoomCommand(String[] params) {
    final String tmp = String.format("%s;%s;%s;%s", TCPCommand.SPAWN_PLAYER_IN_ROOM.getCode(),
        params[GConstant.SESSION_IDX], params[3], params[4]);
    final String sc = tmp.replace(",", ".");
    GSessionManager.getInstance().sendTCPMessage(sc, params[2]);
    log.info("SCSpawnPlayerInRoom: " + sc);
  }

  private void handleSpawnPlayerAndRequirePosInRoomCommand(String[] params) {
    final List<String> playerSessionIds =
        GameManager.getInstance().getAllPlayerSessionInMyRoom(params[GConstant.SESSION_IDX]).stream()
            .filter(item -> !item.equals(params[GConstant.SESSION_IDX]))
            .collect(Collectors.toList());
    final String tmp = String.format("%s;%s;%s;%s", TCPCommand.SPAWN_PLAYER_AND_REQUIRE_POS_IN_ROOM.getCode(),
        params[GConstant.SESSION_IDX], params[2], params[3]);
    final String sc = tmp.replace(",", ".");
    playerSessionIds.forEach(item -> GSessionManager.getInstance().sendTCPMessage(sc, item));
    log.info("SCSpawnPlayerAndRequirePosInRoom(B): " + sc);
  }

  private void handleSpawnPlayerInGameCommand(String[] params) {
    final List<String> playerSessionIds =
        GameManager.getInstance().getAllPlayerSessionInMyRoom(params[GConstant.SESSION_IDX]).stream()
            .filter(item -> !item.equals(params[GConstant.SESSION_IDX]))
            .collect(Collectors.toList());
    final String tmp = String.format("%s;%s;%s;%s;%s", TCPCommand.SPAWN_PLAYER_IN_GAME.getCode(),
        params[GConstant.SESSION_IDX], params[2], params[3], params[4]);
    final String sc = tmp.replace(",", ".");
    playerSessionIds.forEach(item -> GSessionManager.getInstance().sendTCPMessage(sc, item));
    log.info("SCSpawnPlayerInGame(B): " + sc);
  }

  private void handleGetRoomInfoCommand(String[] params) {
    final Room room = GameManager.getInstance().getRoomBySessionId(params[GConstant.SESSION_IDX]);
    if (room == null) return;
    final String sc = String.format("%s;%s;%d;%d;%d;%d", TCPCommand.GET_ROOM_INFO.getCode(),
        room.getCode(), room.getTimeInMinutes(),
        room.getMaxPlayers(), room.getNumOfChicken(), room.isPrivate() ? 1 : 0);
    send2Client(sc);
    log.info("SCGetRoomInfo: " + sc);
  }

  private void handleSetGameCommand(byte[] sc, int len, String[] params) {
    final boolean success = GameManager.getInstance().setGame(params[GConstant.SESSION_IDX],
        Integer.parseInt(params[2]), Integer.parseInt(params[3]),
        Integer.parseInt(params[4]), Integer.parseInt(params[5]));
    if (!success) return;
    final List<String> sessionIds =
        GameManager.getInstance().getAllPlayerSessionInMyRoom(params[GConstant.SESSION_IDX]);
    sessionIds.forEach(item -> GSessionManager.getInstance().sendTCPMessage(sc, len, item));
    log.info("SCSetGame(B)");
  }

  private void handleInShellCommand(byte[] sc, int len, String sessionId) {
    final List<String> sessionIds = GameManager.getInstance().getAllPlayerSessionInMyRoom(sessionId).stream()
        .filter(item -> !item.equals(sessionId))
        .collect(Collectors.toList());
    sessionIds.forEach(item -> GSessionManager.getInstance().sendTCPMessage(sc, len, item));
    log.info("SCInShell(B)");
  }

  private void handleOutShellCommand(byte[] sc, int len, String sessionId) {
    final List<String> sessionIds = GameManager.getInstance().getAllPlayerSessionInMyRoom(sessionId).stream()
        .filter(item -> !item.equals(sessionId))
        .collect(Collectors.toList());
    sessionIds.forEach(item -> GSessionManager.getInstance().sendTCPMessage(sc, len, item));
    log.info("SCOutShell(B)");
  }

  private void handleOutGameCommand(byte[] sc, int len, String sessionId) {
    final List<String> sessionIds = GameManager.getInstance().getAllPlayerSessionInMyRoom(sessionId).stream()
        .filter(item -> !item.equals(sessionId))
        .collect(Collectors.toList());
    GameManager.getInstance().outGame(sessionId);
    sessionIds.forEach(item -> GSessionManager.getInstance().sendTCPMessage(sc, len, sessionId));
    log.info("SCOutGame(B)");
  }

  private void handleOutRoomCommand(String[] params) {
    final String roomCode = GameManager.getInstance().getRoomCode(params[GConstant.SESSION_IDX]);
    final String newHostSessionId = GameManager.getInstance().outRoom(params[GConstant.SESSION_IDX]);

    final List<String> players = GameManager.getInstance().getAllPlayerSessionInMyRoomByCode(roomCode);
    final String sc = String.format("%s;%s;%s",
        TCPCommand.OUT_ROOM.getCode(), params[GConstant.SESSION_IDX], newHostSessionId);
    players.forEach(sessionId -> GSessionManager.getInstance().sendTCPMessage(sc, sessionId));
    log.info("SCOutRoom: " + sc);
  }

  /**
   * CS: code;sessionId
   */
  private void handleDisconnectCommand(String[] params) {
    try {
      GSessionManager.getInstance().disconnect(params[GConstant.SESSION_IDX]);
    } catch (IOException e) {
      throw new NetworkException(e.getMessage());
    }
  }

  private void handleStartGameCommand(String[] params) {
    final List<Player> players = GameManager.getInstance().startGame(params[GConstant.SESSION_IDX]);
//    players.forEach(player -> {
//      final String sc = String.format("%s;%d;%s;%s",
//          TCPCommand.START_GAME.getCode(), player.getIsChicken(), player.getPosX(), player.getPosY());
//      GSessionManager.getInstance().sendTCPMessage(sc, player.getSessionId());
//    });
  }

  private void handleFindRoomCommand(String[] params) {
    final int success = GameManager.getInstance().findRoom(params[GConstant.SESSION_IDX]);
    if (success == 0) return;
    send2Client(TCPCommand.FIND_ROOM.getCode());
    log.info("SCFindRoom: " + TCPCommand.FIND_ROOM.getCode());
  }

  private void handleCreateRoomCommand(String[] params) {
    final boolean success = GameManager.getInstance().createNewRoom(params[GConstant.SESSION_IDX]);
    if (!success) return;
    send2Client(TCPCommand.CREATE_ROOM.getCode());
    log.info("SCCreateRoom: " + TCPCommand.CREATE_ROOM.getCode());
  }

  private void handleJoinRoomCommand(String[] params) {
    final int success = GameManager.getInstance().joinRoom(params[GConstant.SESSION_IDX], params[GConstant.ROOM_CODE_IDX]);
    if (success == 0) return;
    send2Client(TCPCommand.JOIN_ROOM.getCode());
    log.info("SCJoinRoom: " + TCPCommand.JOIN_ROOM.getCode());
  }

  /**
   * CS: code
   * SC: code;sessionId
   */
  private void handleInitSessionCommand() {
    final String sessionId = GSessionManager.getInstance().initSession();
    GSessionManager.getInstance().registerDataOutputStream(sessionId, this);
    final String sc = String.format("%s;%s", TCPCommand.INIT_SESSION.getCode(), sessionId);
    log.info("SCInitSession: {}", sc);
    send2Client(sc);
  }
}
