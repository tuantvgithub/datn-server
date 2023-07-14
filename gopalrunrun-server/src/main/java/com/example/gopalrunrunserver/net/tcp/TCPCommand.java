package com.example.gopalrunrunserver.net.tcp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum TCPCommand {
  CS_INIT_SESSION("c0.0"),
  CS_INIT_UDP("c0.1"),
  CS_DISCONNECT("c0.2"),
  CS_AUTH("c1.0"),
  CS_CREATE_ROOM("c2.0"),
  CS_JOIN_ROOM("c2.1"),
  CS_FIND_ROOM("c2.2"),
  CS_START_GAME("c2.3"),
  CS_OUT_ROOM("c2.4"),

  SC_INIT_SESSION("s0.0"),
  SC_INIT_UDP("s0.1"),
  SC_AUTH("s1.0"),
  SC_CREATE_ROOM("s2.0"),
  SC_JOIN_ROOM("s2.1"),
  SC_FIND_ROOM("s2.2"),
  SC_START_GAME("s2.3"),
  SC_OUT_ROOM("s2.4"),
  SC_IS_HOST("s2.5");
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
