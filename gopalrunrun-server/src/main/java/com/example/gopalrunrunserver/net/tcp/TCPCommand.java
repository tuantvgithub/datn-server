package com.example.gopalrunrunserver.net.tcp;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum TCPCommand {
  INIT_SESSION("0"),
  PING("1"),
  DISCONNECT("2"),
  CREATE_ROOM("3"),
  JOIN_ROOM("4"),
  FIND_ROOM("5"),
  START_GAME("6"),
  END_GAME("7"),
  OUT_ROOM("8"),
  IN_SHELL("9"),
  OUT_SHELL("q"),
  OUT_GAME("w"),
  SET_GAME("e"),
  GET_ROOM_INFO("r"),
  SPAWN_PLAYER_IN_ROOM("t"),
  SPAWN_PLAYER_IN_GAME("y"),
  SPAWN_PLAYER_AND_REQUIRE_POS_IN_ROOM("u"),
  INIT_UDP("i"),
  CLOSE_UDP("o"),
  ;
  private final String code;

  private static final Map<String, TCPCommand> mapCode2TCPCommand = new HashMap<>();

  static {
    loadingMapCode2TCPCommand();
  }

  public static TCPCommand getByCode(String code) {
    return mapCode2TCPCommand.get(code);
  }

  private static void loadingMapCode2TCPCommand() {
    for (TCPCommand command : TCPCommand.values()) {
      mapCode2TCPCommand.put(command.getCode(), command);
    }
  }
}
