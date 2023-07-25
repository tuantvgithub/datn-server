package com.example.gopalrunrunserver.net.netty.tcp;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NettyTcpCommand {
  public static final String INIT_SESSION = "0";
  public static final String DISCONNECT = "3";
  public static final String CREATE_ROOM = "4";
  public static final String JOIN_ROOM = "5";
  public static final String FIND_ROOM = "6";
  public static final String OUT = "7";
  public static final String BROADCAST_SPAWN_PLAYER = "8";
  public static final String SPAWN_AND_REQUIRE_POS_IN_ROOM = "9";
  public static final String START_GAME = "q";
  public static final String FINISHED = "w";
  public static final String IN_SHELL = "e";
  public static final String OUT_SHELL = "r";
  public static final String OUT_GAME = "t";
  public static final String SET_GAME = "y";
  public static final String GET_ROOM = "u";
  public static final String SPAWN_IN_GAME = "i";
}
