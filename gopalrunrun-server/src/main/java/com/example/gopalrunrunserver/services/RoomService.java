package com.example.gopalrunrunserver.services;

import com.example.gopalrunrunserver.models.obj.Player;

import java.util.List;
import java.util.Set;

public interface RoomService {
    int createNewRoom(String sessionId, String mapId, int maxPlayers, int numOfChicken);
    int joinRoom(String sessionId, String roomCode);
    int joinRandomRoom(String sessionId);
    Set<String> getAllPlayerSessionInMyRoom(String sessionId);
    List<String> getAllPlayerSessionInMyRoomByCode(String roomCode);
    boolean isHost(String sessionId);
    List<Player> startGame(String sessionId);
    boolean outRoom(String sessionId);
    void setHost(String sessionId);
    String getRoomCode(String sessionId);
}
