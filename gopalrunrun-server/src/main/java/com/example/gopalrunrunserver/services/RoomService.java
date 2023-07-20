package com.example.gopalrunrunserver.services;

import com.example.gopalrunrunserver.models.obj.Player;
import com.example.gopalrunrunserver.models.obj.Room;

import java.util.List;

public interface RoomService {
    String createNewRoom(String sessionId);
    int joinRoom(String sessionId, String roomCode);
    String joinRandomRoom(String sessionId);
    List<String> getAllPlayerSessionInMyRoom(String sessionId);
    List<String> getAllPlayerSessionInMyRoomByCode(String roomCode);
    boolean isHost(String sessionId);
    List<Player> startGame(String sessionId);
    String outRoom(String sessionId);
    void outGame(String sessionId);
    String getRoomCode(String sessionId);
    boolean setGame(String sessionId, int time, int chickens, int players, int isPrivate);
    Room getRoom(String sessionId);
    void endGame(String sessionId);
}
