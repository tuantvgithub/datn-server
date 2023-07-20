package com.example.gopalrunrunserver.net.udp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum UDPCommand {
  CS_SEND_POS_IN_ROOM("0"),
  CS_BROADCAST_POS_IN_ROOM("1"),
  CS_BROADCAST_MOVE_IN_ROOM("2"),
  CS_SEND_POS_IN_GAME("3"),
  CS_BROADCAST_POS_IN_GAME("4"),
  CS_BROADCAST_MOVE_IN_GAME("5"),
  CS_UPDATE_POS_IN_ROOM("c"),
  CS_UPDATE_POS_IN_GAME("d"),

  SC_SEND_POS_IN_ROOM("6"),
  SC_BROADCAST_POS_IN_ROOM("7"),
  SC_BROADCAST_MOVE_IN_ROOM("8"),
  SC_SEND_POS_IN_GAME("9"),
  SC_BROADCAST_POS_IN_GAME("a"),
  SC_BROADCAST_MOVE_IN_GAME("b"),
  SC_UPDATE_POS_IN_ROOM("e"),
  SC_UPDATE_POS_IN_GAME("f"),
  ;
  private final String code;

  private static final Map<String, UDPCommand> mapCode2UDPCommand = new HashMap<>();

  @Nullable
  public static UDPCommand getByCode(String code) {
    if (mapCode2UDPCommand.isEmpty())
      loadingMapCode2UDPCommand();
    return mapCode2UDPCommand.get(code);
  }

  private static void loadingMapCode2UDPCommand() {
    for (UDPCommand command : UDPCommand.values()) {
      mapCode2UDPCommand.put(command.getCode(), command);
    }
  }
}
