package com.example.gopalrunrunserver.net.tcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum TCPCommand {
  CS_INIT_SESSION("0"),
  CS_INIT_UDP("1"),
  CS_DISCONNECT("2"),
  CS_AUTH("3"),
  CS_CREATE_ROOM("4"),
  CS_JOIN_ROOM("5"),
  CS_FIND_ROOM("6"),
  CS_START_GAME("7"),
  CS_END_GAME("70"),
  CS_OUT_ROOM("8"),
  CS_IN_SHELL("h"),
  CS_OUT_SHELL("i"),
  CS_DIE("m"),
  CS_OUT_GAME("o"),
  CS_UPDATE_POS_IN_ROOM("q"),
  CS_UPDATE_POS_IN_GAME("r"),
  CS_HIT("y"),
  CS_SET_GAME("v"),
  CS_GET_ROOM_INFO("10"),
  CS_SPAWN_PLAYER_IN_ROOM("11"),
  CS_SPAWN_PLAYER_IN_GAME("12"),
  CS_SPAWN_PLAYER_AND_REQUIRE_POS_IN_ROOM("13"),
  CS_PING("00"),

  SC_INIT_SESSION("9"),
  SC_INIT_UDP("a"),
  SC_AUTH("b"),
  SC_CREATE_ROOM("c"),
  SC_JOIN_ROOM("d"),
  SC_FIND_ROOM("e"),
  SC_START_GAME("f"),
  SC_OUT_ROOM("g"),
  SC_IN_SHELL("k"),
  SC_OUT_SHELL("l"),
  SC_DIE("n"),
  SC_OUT_GAME("u"),
  SC_UPDATE_POS_IN_ROOM("w"),
  SC_UPDATE_POS_IN_GAME("t"),
  SC_HIT("p"),
  SC_SET_GAME("z"),
  SC_GET_ROOM_INFO("15"),
  SC_SPAWN_PLAYER_IN_ROOM("16"),
  SC_SPAWN_PLAYER_IN_GAME("17"),
  SC_SPAWN_PLAYER_AND_REQUIRE_POS_IN_ROOM("18"),
  SC_PING("20"),
  ;
  private final String code;

  private static final Map<String, TCPCommand> mapCode2TCPCommand = new HashMap<>();

  @Nullable
  public static TCPCommand getByCode(String code) {
    if (mapCode2TCPCommand.isEmpty())
      loadingMapCode2TCPCommand();
    return mapCode2TCPCommand.get(code);
  }

  private static void loadingMapCode2TCPCommand() {
    for (TCPCommand command : TCPCommand.values()) {
      mapCode2TCPCommand.put(command.getCode(), command);
    }
  }
}
