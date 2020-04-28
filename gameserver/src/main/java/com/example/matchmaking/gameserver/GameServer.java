package com.example.matchmaking.gameserver;

import static com.example.matchmaking.util.ConnectionProtos.*;
import com.example.matchmaking.util.Constants;
import com.example.matchmaking.util.Logger;
import com.example.matchmaking.util.ProtoSocket;
import com.example.matchmaking.util.Tokens;
import com.google.protobuf.ByteString;

import static com.example.matchmaking.util.ProtoSocket.*;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class GameServer {

    private ProtoSocket socket;
    private List<Access> playerAccess;
    private final HashMap<ByteString, Player> players = new HashMap<>();

    public GameServer(String[] args) {
        try {
            socket = new ProtoSocket(Constants.GAME_SERVER_START_PORT, Constants.GAME_SERVER_END_PORT);
        } catch (IOException e) {
            Logger.fatalException(e);
        }
        ByteString token = Tokens.decode(args[0]);

        socket.request(new ProtoPacket<>(
                Request.newBuilder()
                        .setType(MessageType.REGISTER_GAME)
                        .setAccess(Access.newBuilder()
                                .setToken(token)
                                .build()
                        )
                        .build(),
                Constants.MATCHMAKING_SERVER
        ), this::onRegister);
    }

    public void onRegister(Response responseProto) {
        playerAccess = responseProto.getPlayerAccessList();
        socket.listen(MessageType.JOIN, this::onJoin, this::onJoinFinish, Constants.JOIN_TIMEOUT);
    }

    private boolean checkAccessDenied(ProtoPacket<Request> requestPacket) {
        for (Access access : playerAccess) {
            if(new Location(access).equals(requestPacket.getLocation()) &&
                    access.getToken().equals(requestPacket.getMessage().getAccess().getToken())) {
                return false;
            }
        }
        return true;
    }

    public Response onJoin(ProtoPacket<Request> requestPacket) {
        if(checkAccessDenied(requestPacket)) return Response.newBuilder().setType(MessageType.JOIN).setSuccess(false).build();

        players.put(requestPacket.getMessage().getAccess().getToken(), requestPacket.getMessage().getPlayer()); // TODO validate position

        return Response.newBuilder()
                .setType(MessageType.JOIN)
                .setSuccess(true)
                .addAllPlayers(players.values())
                .build();
    }

    public void onJoinFinish(boolean timeout) {
        if(timeout)
            Logger.fatalException(new SocketTimeoutException("nobody joined the game"));
        else
            socket.listen(MessageType.UPDATE, this::onUpdate, this::onGameFinish, Constants.GAME_LENGTH);
    }

    public Response onUpdate(ProtoPacket<Request> requestPacket) {
        if(checkAccessDenied(requestPacket)) return Response.newBuilder().setType(MessageType.JOIN).setSuccess(false).build();

        players.put(requestPacket.getMessage().getAccess().getToken(), requestPacket.getMessage().getPlayer()); // TODO validate position

        return Response.newBuilder()
                .setType(MessageType.UPDATE)
                .addAllPlayers(players.values())
                .setSuccess(true)
                .build();
    }

    public void onGameFinish(boolean timeout) {
        for (Access access : playerAccess) {
            socket.request(new ProtoPacket<>(
                            Request.newBuilder()
                                .setType(MessageType.LEAVE)
                                .build(),
                            new Location(access)
                    ),
                    (ignored) -> {}
            );
        }
        try {
            Thread.sleep(Constants.LEAVE_TIMEOUT);
        } catch (InterruptedException e) {
            Logger.fatalException(e);
        }
        System.exit(0);
    }

}
