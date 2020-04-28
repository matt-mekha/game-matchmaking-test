package com.example.matchmaking.matchmaker;

import static com.example.matchmaking.util.ConnectionProtos.*;
import com.example.matchmaking.util.Constants;
import com.example.matchmaking.util.Logger;
import com.example.matchmaking.util.ProtoSocket;

import java.io.IOException;

public class MatchmakingServer {

    private ProtoSocket protoSocket;
    private final ClientQueue queue = new ClientQueue(Constants.PLAYERS_PER_GAME);

    public MatchmakingServer() {
        try {
            protoSocket = new ProtoSocket(Constants.MATCHMAKING_SERVER.getPort());
        } catch (IOException e) {
            e.printStackTrace();
        }

        protoSocket.listen(MessageType.QUEUE, this::onQueue);
        Logger.log("UP AND RUNNING");
    }

    public Response onQueue(ProtoSocket.ProtoPacket<Request> requestProto) {
        boolean ready = queue.add(requestProto.getLocation());
        if(ready) {
            Logger.log("queue is ready for a match");
            // TODO spawn game server
        }
        return Response.newBuilder()
                .setType(MessageType.QUEUE)
                .setSuccess(true)
                .build();
    }
}
