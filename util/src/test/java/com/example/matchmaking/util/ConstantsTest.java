package com.example.matchmaking.util;

import com.google.protobuf.ByteString;
import com.google.protobuf.Message;
import org.junit.Test;
import static org.junit.Assert.*;

public class ConstantsTest {

    @Test
    public void screenRatioTest() {
        assertEquals(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT * 16 / 9);
    }

    private void assertFits(Message message) {
        assertTrue(Constants.PACKET_LENGTH >= Constants.PACKET_HEADER_LENGTH + message.getSerializedSize());
    }

    @Test
    public void packetSizeTest() {
        ConnectionProtos.Player bigPlayer = ConnectionProtos.Player.newBuilder()
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

        ConnectionProtos.Request bigRequest = ConnectionProtos.Request.newBuilder()
                .setType(ConnectionProtos.MessageType.UPDATE)
                .setPlayer(bigPlayer)
                .setAccess(ConnectionProtos.Access.newBuilder()
                        .setToken(Tokens.generate())
                        .build()
                )
                .build();

        ConnectionProtos.Response.Builder bigResponseBuilder = ConnectionProtos.Response.newBuilder()
                .setType(ConnectionProtos.MessageType.UPDATE)
                .setSuccess(true);
                for (int i = 0; i < Constants.PLAYERS_PER_GAME; i++) {
                    bigResponseBuilder.addPlayers(bigPlayer);
                }
        ConnectionProtos.Response bigResponse = bigResponseBuilder.build();

        ConnectionProtos.Response.Builder bigResponseBuilder2 = ConnectionProtos.Response.newBuilder()
                .setType(ConnectionProtos.MessageType.UPDATE)
                .setSuccess(true);
                for (int i = 0; i < Constants.PLAYERS_PER_GAME; i++) {
                    bigResponseBuilder2.addPlayerAccess(ConnectionProtos.Access.newBuilder()
                            .setToken(Tokens.generate())
                            .setAddress(ByteString.copyFrom(new byte[] {1, 1, 1, 1}))
                            .setPort(65535)
                            .build()
                    );
                }
        ConnectionProtos.Response bigResponse2 = bigResponseBuilder2.build();


        assertFits(bigRequest);
        assertFits(bigResponse);
        assertFits(bigResponse2);
    }

}
