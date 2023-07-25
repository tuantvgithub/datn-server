package com.example.gopalrunrunserver.net.udp;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum UDPCommand {
  BROADCAST_MOVE_IN_ROOM("0"),
  BROADCAST_MOVE_IN_GAME("1"),
  UPDATE_POS_IN_ROOM("2"),
  UPDATE_POS_IN_GAME("3"),
  INIT("4"),
  ;
  private final String code;

  private static final Map<String, UDPCommand> mapCode2UDPCommand = new HashMap<>();

  static {
    loadingMapCode2UDPCommand();
  }

  public static UDPCommand getByCode(String code) {
    return mapCode2UDPCommand.get(code);
  }

  private static void loadingMapCode2UDPCommand() {
    for (UDPCommand command : UDPCommand.values()) {
      mapCode2UDPCommand.put(command.getCode(), command);
    }
  }
}
