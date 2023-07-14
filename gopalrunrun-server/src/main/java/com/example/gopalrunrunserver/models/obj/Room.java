package com.example.gopalrunrunserver.models.obj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private String code;
    private String hostSessionId;
    private boolean started;

    private String mapId;
    private boolean isPrivate = true;
    private int maxPlayers = 12;
    private int numOfChicken = 3;
    private Set<String> playerSessions = new HashSet<>();

    private long createdAt = System.currentTimeMillis();
}
