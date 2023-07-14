package com.example.gopalrunrunserver.net.udp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public enum UDPCommand {
  CS_SEND_POS_IN_ROOM("c0"),
  CS_BROADCAST_POS_IN_ROOM("c1"),
  CS_BROADCAST_MOVE_IN_ROOM("c2"),
  CS_SEND_POS_IN_GAME("c4"),
  CS_BROADCAST_POS_IN_GAME("c5"),
  CS_BROADCAST_MOVE_IN_GAME("c3"),

  SC_SEND_POS_IN_ROOM("s0"),
  SC_BROADCAST_POS_IN_ROOM("s1"),
  SC_BROADCAST_MOVE_IN_ROOM("s2"),
  SC_SEND_POS_IN_GAME("s4"),
  SC_BROADCAST_POS_IN_GAME("s5"),
  SC_BROADCAST_MOVE_IN_GAME("s3"),
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
