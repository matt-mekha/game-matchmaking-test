package com.example.matchmaking.matchmaker;

import static com.example.matchmaking.util.ConnectionProtos.*;
import static com.example.matchmaking.util.ProtoSocket.*;
import com.example.matchmaking.util.Constants;
import com.example.matchmaking.util.Logger;
import com.example.matchmaking.util.ProtoSocket;
import com.example.matchmaking.util.Tokens;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.util.ArrayList;

public class MatchmakingServer {

    private ProtoSocket socket;
    private final ClientQueue queue = new ClientQueue(Constants.PLAYERS_PER_GAME);

    public MatchmakingServer() {
        try {
            socket = new ProtoSocket(Constants.MATCHMAKING_SERVER.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        socket.listen(MessageType.QUEUE, this::onQueue);
        Logger.log("UP AND RUNNING");
    }

    public Response onQueue(ProtoPacket<Request> requestProto) {
        boolean ready = queue.add(requestProto.getLocation());
        if(ready) {
            new Thread(this::spawnServer).start();
        }
        return Response.newBuilder()
                .setType(MessageType.QUEUE)
                .setSuccess(true)
                .build();
    }

    public void spawnServer() {
        Logger.log("spawning a game server");

        ByteString gameServerToken = Tokens.generate();

        ArrayList<Location> playerLocations = queue.get();
        ArrayList<Access> playerAccess = new ArrayList<>();

        for (Location playerLocation : playerLocations) {
            playerAccess.add(playerLocation.toProto().toBuilder()
                    .setToken(Tokens.generate())
                    .build()
            );
        }

        socket.listen(MessageType.REGISTER_GAME, (requestPacket) -> {
            if(requestPacket.getMessage().getAccess().getToken().equals(gameServerToken)) {
                Logger.log("successfully spawned game server");
                for (Access access : playerAccess) {
                    socket.request(
                            new ProtoPacket<>(
                                    Request.newBuilder()
                                            .setType(MessageType.MATCH)
                                            .setAccess(requestPacket.getLocation().toProto().toBuilder()
                                                    .setToken(access.getToken())
                                                    .build()
                                            )
                                            .build(),
                                    new Location(access)
                            ),
                            response -> {}
                    );
                }
                return Response.newBuilder()
                        .setType(MessageType.REGISTER_GAME)
                        .addAllPlayerAccess(playerAccess)
                        .setSuccess(true)
                        .build();
            } else {
                return Response.newBuilder()
                        .setType(MessageType.REGISTER_GAME)
                        .setSuccess(false)
                        .build();
            }
        }, (timeout) -> {
            if(timeout) Logger.fatalException(new IOException("failed to spawn game server"));
        }, Constants.GAME_SERVER_SPAWN_TIMEOUT);

        try {
            Runtime.getRuntime().exec(String.format("java -jar %s.jar %s", Constants.GAME_SERVER_JAR_NAME, Tokens.encode(gameServerToken)));
        } catch (IOException e) {
            Logger.fatalException(e);
        }
    }
}
