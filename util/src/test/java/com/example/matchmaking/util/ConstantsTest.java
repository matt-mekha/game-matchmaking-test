package com.example.matchmaking.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class ConstantsTest {

    @Test
    public void screenRatioTest() {
        assertEquals(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT * 16 / 9);
    }

    @Test
    public void packetSizeTest() {
        ConnectionProtos.Player biggestPlayer = ConnectionProtos.Player.newBuilder()
                .setPosition(ConnectionProtos.Player.Position.newBuilder()
                        .setX(1)
                        .setY(1)
                )
                .setColor(ConnectionProtos.Player.Color.newBuilder()
                        .setR(1)
                        .setG(1)
                        .setB(1)
                )
                .build();

        ConnectionProtos.Request biggestRequest = ConnectionProtos.Request.newBuilder()
                .setType(ConnectionProtos.MessageType.UPDATE)
                .setPlayer(biggestPlayer)
                .setToken(Tokens.generate())
                .build();

        ConnectionProtos.Response.Builder biggestResponseBuilder = ConnectionProtos.Response.newBuilder()
                .setType(ConnectionProtos.MessageType.UPDATE)
                .setSuccess(true);

        for (int i = 0; i < Constants.PLAYERS_PER_GAME; i++) {
            biggestResponseBuilder.addPlayers(biggestPlayer);
        }

        ConnectionProtos.Response biggestResponse = biggestResponseBuilder.build();

        assertTrue(Constants.PACKET_LENGTH >= Constants.PACKET_HEADER_LENGTH + biggestRequest.getSerializedSize());
        assertTrue(Constants.PACKET_LENGTH >= Constants.PACKET_HEADER_LENGTH + biggestResponse.getSerializedSize());
    }

}
