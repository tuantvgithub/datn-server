package com.example.gopalrunrunserver.net.netty.tcp;

import com.example.gopalrunrunserver.consts.GConstant;
import com.example.gopalrunrunserver.game.GameManager;
import com.example.gopalrunrunserver.models.obj.Player;
import com.example.gopalrunrunserver.models.obj.Room;
import com.example.gopalrunrunserver.net.exceptions.NetworkException;
import com.example.gopalrunrunserver.net.netty.udp.NettyUdpServer;
import com.example.gopalrunrunserver.utils.GStringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class TcpServerHandler extends ChannelInboundHandlerAdapter {
  @Override
  public void channelRead(ChannelHandlerContext ctx, Object msg) {
    final ByteBuf in = (ByteBuf) msg;
    try {
      final String cs = in.toString(CharsetUtil.UTF_8);
      handleMessage(ctx, cs);
    } catch (NetworkException e) {
      log.error(e.getMessage());
    } catch (Exception ignore) {

    } finally {
      in.release();
    }
  }

  private void sendMessage2Client(ChannelHandlerContext ctx, String msg) {
    ctx.writeAndFlush(Unpooled.copiedBuffer(msg, CharsetUtil.UTF_8));
  }

  private void handleMessage(ChannelHandlerContext ctx, String cs) {
    final String[] params = cs.split(GConstant.DATA_SEPARATED);
    final String command = params[GConstant.COMMAND_IDX];
    if (NettyTcpCommand.INIT_SESSION.equals(command)) {
      log.info("cs-init-session: " + cs);
      initSession(ctx);
      return;
    }
    final String sessionId = params[GConstant.SESSION_IDX];
    if (!NettyTcpServer.getInstance().isValidSession(sessionId)) {
      throw new NetworkException("invalid session: " + sessionId);
    }

    if (NettyTcpCommand.DISCONNECT.equals(command)) {
      log.info("cs-disconnect: " + cs);
      disconnect(sessionId);
      return;
    }
    if (NettyTcpCommand.CREATE_ROOM.equals(command)) {
      log.info("cs-create: " + cs);
      createRoom(ctx, sessionId);
      return;
    }
    if (NettyTcpCommand.JOIN_ROOM.equals(command)) {
      log.info("cs-join: " + cs);
      joinRoom(ctx, sessionId, params[GConstant.ROOM_CODE_IDX]);
      return;
    }
    if (NettyTcpCommand.FIND_ROOM.equals(command)) {
      log.info("cs-find: " + cs);
      findRoom(ctx, sessionId);
      return;
    }
    if (NettyTcpCommand.BROADCAST_SPAWN_PLAYER.equals(command)) {
      log.info("cs-bc-spawn: " + cs);
      spawnInRoom(sessionId, cs);
      return;
    }
    if (NettyTcpCommand.GET_ROOM.equals(command)) {
      log.info("cs-get-r: " + cs);
      getRoom(ctx, sessionId);
    }
    if (NettyTcpCommand.OUT.equals(command)) {
      log.info("cs-out: " + cs);
      out(sessionId, cs);
    }
    if (NettyTcpCommand.START_GAME.equals(command)) {
      log.info("cs-start: " + cs);
      startGame(sessionId);
    }
    if (NettyTcpCommand.SET_GAME.equals(command)) {
      log.info("cs-set-r: " + cs);
      setGame(sessionId, params);
    }
  }

  private void setGame(String sessionId, String[] params) {
    if (!GameManager.getInstance().isHost(sessionId)) return;
    final boolean success = GameManager.getInstance().setGame(params[GConstant.SESSION_IDX],
            Integer.parseInt(params[2]), Integer.parseInt(params[3]),
            Integer.parseInt(params[4]), Integer.parseInt(params[5]));
    if (!success) return;
    final String sc = NettyTcpCommand.SET_GAME + GConstant.DATA_SEPARATED +
            params[2] + GConstant.DATA_SEPARATED + params[3] + GConstant.DATA_SEPARATED +
            params[4] + GConstant.DATA_SEPARATED + params[5];
    NettyTcpServer.getInstance().broadcast(sc, GameManager.getInstance().getAllPlayerSessionInMyRoom(sessionId));
  }

  private void startGame(String sessionId) {
    final List<Player> players = GameManager.getInstance().startGame(sessionId);
    players.parallelStream().forEach(player -> {
      final String sc = NettyTcpCommand.START_GAME + GConstant.DATA_SEPARATED + player.getIsSnail() +
              GConstant.DATA_SEPARATED + player.getPosX() + GConstant.DATA_SEPARATED + player.getPosY();
      NettyTcpServer.getInstance().sendMessage2Client(sc, player.getSessionId());
    });
  }

  private void getRoom(ChannelHandlerContext ctx, String sessionId) {
    final Room room = GameManager.getInstance().getRoomBySessionId(sessionId);
    if (room == null) return;
    final String sc = NettyTcpCommand.GET_ROOM + GConstant.DATA_SEPARATED + room.getCode() + GConstant.DATA_SEPARATED
        + room.getTimeInMinutes() + GConstant.DATA_SEPARATED + room.getMaxPlayers() + GConstant.DATA_SEPARATED
        + room.getNumOfChicken() + GConstant.DATA_SEPARATED + (room.isPrivate() ? 1 : 0);
    sendMessage2Client(ctx, sc);
    log.info("sc-get-r: " + sc);
  }

  private void spawnInRoom(String sessionId, String sc) {
    NettyTcpServer.getInstance().broadcast(sc,
            GameManager.getInstance().getOtherPlayerSessionInMyRoom(sessionId));
    log.info("sc-spawn-r: " + sc);
  }

  private void out(String sessionId, String cs) {
    final List<String> players = GameManager.getInstance().getAllPlayerSessionInMyRoom(sessionId);
    final String newHostSession = GameManager.getInstance().outRoom(sessionId);
    final String broadcastSc = cs + GConstant.DATA_SEPARATED + newHostSession;
    NettyTcpServer.getInstance().broadcast(broadcastSc, players);
  }

  private void findRoom(ChannelHandlerContext ctx, String sessionId) {
    final int code = GameManager.getInstance().findRoom(sessionId);
    final String sc = NettyTcpCommand.FIND_ROOM + GConstant.DATA_SEPARATED + code;
    sendMessage2Client(ctx, sc);
    log.info("sc-find: " + sc);
  }

  private void joinRoom(ChannelHandlerContext ctx, String sessionId, String roomCode) {
    final int code = GameManager.getInstance().joinRoom(sessionId, roomCode);
    final String sc = NettyTcpCommand.JOIN_ROOM + GConstant.DATA_SEPARATED + code;
    sendMessage2Client(ctx, sc);
    log.info("sc-join: " + sc);
  }

  private void createRoom(ChannelHandlerContext ctx, String sessionId) {
    final int code = GameManager.getInstance().createRoom(sessionId);
    final String sc = NettyTcpCommand.CREATE_ROOM + GConstant.DATA_SEPARATED + code;
    sendMessage2Client(ctx, sc);
    log.info("sc-create: " + sc);
  }

  private void disconnect(String sessionId) {
    NettyTcpServer.getInstance().removeClient(sessionId);
    NettyUdpServer.getInstance().removeClient(sessionId);
  }

  private void initSession(ChannelHandlerContext ctx) {
    final String sessionId = GStringUtils.random(3);
    if (NettyTcpServer.getInstance().sessionExists(sessionId)) initSession(ctx);

    NettyTcpServer.getInstance().addClient(sessionId, ctx.channel());
    final String sc = NettyTcpCommand.INIT_SESSION + GConstant.DATA_SEPARATED + sessionId;
    sendMessage2Client(ctx, sc);
    log.info("sc-init-session: " + sc);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    log.error("netty tcp error", cause);
  }
}
