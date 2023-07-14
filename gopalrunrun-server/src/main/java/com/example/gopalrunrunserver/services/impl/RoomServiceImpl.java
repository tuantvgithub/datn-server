package com.example.gopalrunrunserver.services.impl;

import com.example.gopalrunrunserver.models.obj.Player;
import com.example.gopalrunrunserver.models.obj.Room;
import com.example.gopalrunrunserver.services.RoomService;
import com.example.gopalrunrunserver.utils.GStringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomServiceImpl implements RoomService {
    private final Map<String, Room> mapCode2Room = new ConcurrentHashMap<>();
    private final Map<String, String> mapSessionId2RoomCode = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> mapRoomCode2SessionIds = new ConcurrentHashMap<>();

    @Override
    public int createNewRoom(String sessionId, String mapId, int maxPlayers, int numOfChicken) {
        final Room room = new Room();
        final String code = GStringUtils.random(10);
        if (mapCode2Room.containsKey(code))
            return createNewRoom(sessionId, mapId, maxPlayers, numOfChicken);

        room.setCode(code);
        room.setMapId(mapId);
        room.setMaxPlayers(maxPlayers);
        room.setNumOfChicken(numOfChicken);
        room.getPlayerSessions().add(sessionId);
        room.setHostSessionId(sessionId);

        mapCode2Room.put(code, room);
        mapSessionId2RoomCode.put(sessionId, code);
        mapRoomCode2SessionIds.computeIfAbsent(code, k -> new HashSet<>());
        mapRoomCode2SessionIds.get(code).add(sessionId);

        log.info("RoomCode: " + code);
        return 1;
    }

    @Override
    public int joinRoom(String sessionId, String roomCode) {
        if (!mapCode2Room.containsKey(roomCode)) return 0;
        final Room room = mapCode2Room.get(roomCode);
        if (room.isStarted() || room.getPlayerSessions().size() >= room.getMaxPlayers()) return 0;

        room.getPlayerSessions().add(sessionId);

        mapCode2Room.put(roomCode, room);
        mapSessionId2RoomCode.put(sessionId, roomCode);
        mapRoomCode2SessionIds.get(roomCode).add(sessionId);

        return 1;
    }

    @Override
    public int joinRandomRoom(String sessionId) {
        final List<Room> rooms = mapCode2Room.values().stream()
                .filter(room -> room.getMaxPlayers() > room.getPlayerSessions().size() &&
                        !room.isStarted())
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(rooms)) return 0;

        final Room room = rooms.get(0);
        room.getPlayerSessions().add(sessionId);

        mapCode2Room.put(room.getCode(), room);
        mapSessionId2RoomCode.put(sessionId, room.getCode());
        mapRoomCode2SessionIds.get(room.getCode()).add(sessionId);
        return 1;
    }

    @Override
    public Set<String> getAllPlayerSessionInMyRoom(String sessionId) {
        if (!mapSessionId2RoomCode.containsKey(sessionId)) return Collections.emptySet();
        return mapRoomCode2SessionIds.get(mapSessionId2RoomCode.get(sessionId));
    }

    @Override
    public List<String> getAllPlayerSessionInMyRoomByCode(String roomCode) {
        if (!mapRoomCode2SessionIds.containsKey(roomCode)) return Collections.emptyList();
        return new ArrayList<>(mapRoomCode2SessionIds.get(roomCode));
    }

    @Override
    public boolean isHost(String sessionId) {
        if (!mapSessionId2RoomCode.containsKey(sessionId)) return false;
        final String roomCode = mapSessionId2RoomCode.get(sessionId);
        return mapCode2Room.get(roomCode).getHostSessionId().equals(sessionId);
    }

    @Override
    public List<Player> startGame(String sessionId) {
        if (!isHost(sessionId)) return Collections.emptyList();
        final Room room = mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));
        room.setStarted(true);
        mapCode2Room.put(room.getCode(), room);
        return room.getPlayerSessions().stream()
                .map(s -> {
                    final Player player = new Player();
                    player.setSessionId(s);
                    if (room.getHostSessionId().equals(s))
                        player.setIsChicken(1);
                    player.setPosX(2f);
                    player.setPosY(-5f);
                    return player;
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean outRoom(String sessionId) {
        if (!mapSessionId2RoomCode.containsKey(sessionId)) return false;
        final Room room = mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));

        room.getPlayerSessions().remove(sessionId);

        mapSessionId2RoomCode.remove(sessionId);
        mapRoomCode2SessionIds.put(room.getCode(), room.getPlayerSessions());
        mapCode2Room.put(room.getCode(), room);
        return true;
    }

    @Override
    public void setHost(String sessionId) {
        if (!mapSessionId2RoomCode.containsKey(sessionId)) return;
        final Room room = mapCode2Room.get(mapSessionId2RoomCode.get(sessionId));
        room.setHostSessionId(sessionId);
        mapCode2Room.put(room.getCode(), room);
    }

    @Override
    public String getRoomCode(String sessionId) {
        return mapSessionId2RoomCode.get(sessionId);
    }
}
