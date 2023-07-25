package com.example.gopalrunrunserver.game;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.models.obj.Player;
import com.example.gopalrunrunserver.models.obj.Room;
import com.example.gopalrunrunserver.utils.GRandomUtils;
import com.example.gopalrunrunserver.utils.GStringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
public class GameManager {
  private final Map<String, Room> mapCode2Room = new ConcurrentHashMap<>();
  private final Map<String, String> mapSessionId2RoomCode = new ConcurrentHashMap<>();
  private static final float[] CHICKEN_POS_X = {14f, 15f, 16f, 17f, 18f, 19f, 20f, 21f, 22f, 23f};
  private static final float[] CHICKEN_POS_Y = {9.4f, 9.4f, 9.4f, 9.4f, 9.4f, 9.4f, 9.4f, 9.4f, 9.4f, 9.4f};
  private static final float[] SNAIL_POS_X = {24f, 24f, 25f, 25f, 26f, 26f, 27f, 28f, 29f, 30f};
  private static final float[] SNAIL_POS_Y = {-23f, -22f, -21f, -20f, -23f, -22f, -21f, -20f, -23f, -22f};
  private static final int NUM_OF_POS = 10;

  private static GameManager instance;

  private GameManager() {
  }

  public static GameManager getInstance() {
    if (instance == null) instance = new GameManager();
    return instance;
  }

  public boolean createNewRoom(String sessionId) {
    final Room room = new Room();
    final String code = GStringUtils.random(GConstant.ROOM_CODE_LENGTH);
    if (mapCode2Room.containsKey(code))
      return createNewRoom(sessionId);

    room.setCode(code);
    room.getPlayerSessions().add(sessionId);
    room.setHostSessionId(sessionId);

    mapCode2Room.put(code, room);
    mapSessionId2RoomCode.put(sessionId, code);
    return true;
  }

  public int joinRoom(String sessionId, String roomCode) {
    try {
      final Room room = mapCode2Room.get(roomCode);
      if (room.isStarted() || room.getPlayerSessions().size() >= room.getMaxPlayers()) return 0;
      room.getPlayerSessions().add(sessionId);

      mapCode2Room.put(roomCode, room);
      mapSessionId2RoomCode.put(sessionId, roomCode);
      return 1;
    } catch (Exception e) {
      return 0;
    }
  }

  public int findRoom(String sessionId) {
    try {
      final List<Room> rooms = mapCode2Room.values().stream()
          .filter(room -> room.getMaxPlayers() > room.getPlayerSessions().size() &&
              !room.isStarted() && !room.isPrivate())
          .collect(Collectors.toList());
      if (CollectionUtils.isEmpty(rooms)) return 0;
      final Room room = rooms.get(GRandomUtils.generateRandomNumber(rooms.size() - 1));
      room.getPlayerSessions().add(sessionId);

      mapCode2Room.put(room.getCode(), room);
      mapSessionId2RoomCode.put(sessionId, room.getCode());
      return 1;
    } catch (Exception e) {
      return 0;
    }
  }

  public List<String> getAllPlayerSessionInMyRoom(String sessionId) {
    if (!mapSessionId2RoomCode.containsKey(sessionId)) return Collections.emptyList();
    return mapCode2Room.get(mapSessionId2RoomCode.get(sessionId)).getPlayerSessions();
  }

  public List<String> getOtherPlayerSessionInMyRoom(String sessionId) {
    return getAllPlayerSessionInMyRoom(sessionId).stream()
        .filter(item -> !item.equals(sessionId))
        .collect(Collectors.toList());
  }

  public List<String> getAllPlayerSessionInMyRoomByCode(String roomCode) {
    if (!mapCode2Room.containsKey(roomCode)) return Collections.emptyList();
    return mapCode2Room.get(roomCode).getPlayerSessions();
  }

  public boolean isHost(String sessionId) {
    if (!mapSessionId2RoomCode.containsKey(sessionId)) return false;
    final String roomCode = mapSessionId2RoomCode.get(sessionId);
    return mapCode2Room.get(roomCode).getHostSessionId().equals(sessionId);
  }

  public List<Player> startGame(String sessionId) {
    if (!isHost(sessionId)) return Collections.emptyList();
    final Room room = mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));
    room.setStarted(true);
    mapCode2Room.put(room.getCode(), room);

    final List<String> chickenSessionIds =
        GRandomUtils.selectRandomStrings(room.getPlayerSessions(), room.getNumOfChicken());
    return room.getPlayerSessions().stream()
        .map(s -> {
          final Player player = new Player();
          player.setSessionId(s);
          if (chickenSessionIds.contains(s)) player.setIsSnail(0);
          final int posIdx = GRandomUtils.generateRandomNumber(NUM_OF_POS - 1);
          if (player.getIsSnail() == 0) {
            player.setPosX(CHICKEN_POS_X[posIdx]);
            player.setPosY(CHICKEN_POS_Y[posIdx]);
          } else {
            player.setPosX(SNAIL_POS_X[posIdx]);
            player.setPosY(SNAIL_POS_Y[posIdx]);
          }
          return player;
        })
        .collect(Collectors.toList());
  }

  public String outRoom(String sessionId) {
    final boolean isHost = isHost(sessionId);
    final Room room = mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));
    room.getPlayerSessions().remove(sessionId);
    mapSessionId2RoomCode.remove(sessionId);

    if (CollectionUtils.isEmpty(room.getPlayerSessions())) {
      mapCode2Room.remove(room.getCode());
      return "";
    }
    if (isHost) {
      room.setHostSessionId(room.getPlayerSessions().get(0));
    }
    mapCode2Room.put(room.getCode(), room);
    return room.getHostSessionId();
  }

  public void outGame(String sessionId) {
    final Room room = mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));
    room.getPlayerSessions().remove(sessionId);
    mapSessionId2RoomCode.remove(sessionId);
    mapCode2Room.put(room.getCode(), room);
  }

  public String getRoomCode(String sessionId) {
    return mapSessionId2RoomCode.get(sessionId);
  }

  public boolean setGame(String sessionId, int time, int maxPlayers, int numOfChickens, int isPrivate) {
    if (!isHost(sessionId)) return false;
    final Room room = mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));
    room.setTimeInMinutes(time);
    room.setNumOfChicken(numOfChickens);
    room.setMaxPlayers(maxPlayers);
    room.setPrivate(isPrivate == 1);
    mapCode2Room.put(room.getCode(), room);
    return true;
  }

  public Room getRoomBySessionId(String sessionId) {
    return mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));
  }

  public void endGame(String sessionId) {
    if (!isHost(sessionId)) return;
    final Room room = mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));
    room.setStarted(false);
    mapCode2Room.put(room.getCode(), room);
  }

  public int createRoom(String sessionId) {
    try {
      final Room room = new Room();
      final String code = GStringUtils.random(GConstant.ROOM_CODE_LENGTH);
      if (mapCode2Room.containsKey(code))
        return createRoom(sessionId);

      room.setCode(code);
      room.getPlayerSessions().add(sessionId);
      room.setHostSessionId(sessionId);

      mapCode2Room.put(code, room);
      mapSessionId2RoomCode.put(sessionId, code);
      return 1;
    } catch (Exception e) {
      return 0;
    }
  }
}
