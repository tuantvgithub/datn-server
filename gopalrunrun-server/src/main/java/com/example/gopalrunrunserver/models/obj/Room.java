package com.example.gopalrunrunserver.models.obj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room {
    private String code;
    private String hostSessionId;
    private boolean started;
    private int mapId;

    private int timeInMinutes = 5;
    private boolean isPrivate = true;
    private int maxPlayers = 12;
    private int numOfChicken = 1;
    private List<String> playerSessions = new ArrayList<>();
}
