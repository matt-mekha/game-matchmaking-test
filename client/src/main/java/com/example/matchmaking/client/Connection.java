package com.example.matchmaking.client;

import static com.example.matchmaking.util.ConnectionProtos.*;

import com.example.matchmaking.util.ConnectionProtos;
import com.example.matchmaking.util.Constants;
import com.example.matchmaking.util.Logger;
import com.example.matchmaking.util.ProtoSocket;
import static com.example.matchmaking.util.ProtoSocket.*;

import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Connection {

    private Status status;
    private final ProtoSocket protoSocket = new ProtoSocket();

    private final List<ConnectionProtos.Player> players = new ArrayList<>();
    private final Game game;

    private Location gameServerLocation;
    private boolean gameRunning;

    public Connection(Game game, String[] args) {
        this.game = game;
        if(args.length > 0) {
            try {
                InetAddress newAddress = InetAddress.getByName(args[0]);
                Constants.MATCHMAKING_SERVER.setAddress(newAddress);
            } catch(UnknownHostException e) {
                Logger.log("invalid IP address argument");
            }
        }

        findMatch();
    }

    private void findMatch() {
        status = Status.CONNECTING;

        protoSocket.request(
                new ProtoPacket<>(
                        Request.newBuilder().setType(MessageType.QUEUE).build(),
                        Constants.MATCHMAKING_SERVER
                ),
                this::listenForMatch
        );
    }

    public void listenForMatch(Response responseProto) {
        if(status != Status.CONNECTING) return;

        assertSuccess(responseProto);
        status = Status.QUEUED;

        protoSocket.listen(
                MessageType.MATCH,
                this::joinMatch,
                () -> Logger.fatalException(new SocketTimeoutException("queue timed out")),
                Constants.QUEUE_TIMEOUT
        );
    }

    public Response joinMatch(ProtoPacket<Request> requestProto) {
        if(status != Status.CONNECTING && status != Status.QUEUED) {
            return Response.newBuilder()
                    .setType(MessageType.MATCH)
                    .setSuccess(true)
                    .build();
        }

        status = Status.JOINING;

        gameServerLocation = new Location(requestProto.getMessage().getGameServerLocation());
        protoSocket.request(
                new ProtoPacket<>(
                        Request.newBuilder()
                                .setType(MessageType.JOIN)
                                .setPlayer(game.getMyPlayer())
                                .build(),
                        gameServerLocation
                ),
                this::onJoin
        );

        return Response.newBuilder()
                .setType(MessageType.MATCH)
                .setSuccess(true)
                .build();
    }

    public void onJoin(Response responseProto) {
        assertSuccess(responseProto);
        status = Status.GAME;
        gameRunning = true;

        protoSocket.listen(MessageType.LEAVE, this::onLeave, this::onLeaveTimeout, Constants.GAME_LENGTH);

        while(gameRunning) {
            protoSocket.request(
                    new ProtoPacket<>(
                            Request.newBuilder()
                                    .setType(MessageType.UPDATE)
                                    .setPlayer(game.getMyPlayer())
                                    .build(),
                            gameServerLocation
                    ),
                    this::onUpdate
            );
            try {
                Thread.sleep(Constants.GAME_UPDATE_INTERVAL);
            } catch (InterruptedException e) {
                Logger.fatalException(e);
            }
        }
        findMatch();
    }

    public void onUpdate(Response responseProto) {
        assertSuccess(responseProto);

        players.clear();
        players.addAll(responseProto.getPlayersList());
    }

    public Response onLeave(ProtoPacket<Request> requestProto) {
        gameRunning = false;

        return Response.newBuilder()
                .setType(MessageType.LEAVE)
                .setSuccess(true)
                .build();
    }

    public void onLeaveTimeout() {
        gameRunning = false;
    }

    private void assertSuccess(Response responseProto) {
        assert responseProto.getSuccess();
    }

    public Status getStatus() {
        return status;
    }

    public List<ConnectionProtos.Player> getPlayers() {
        return players;
    }

    public void close() {
    }

    public enum Status {
        CONNECTING,
        QUEUED,
        JOINING,
        GAME,
    }
}
