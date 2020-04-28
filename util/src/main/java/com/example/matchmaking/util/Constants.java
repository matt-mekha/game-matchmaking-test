package com.example.matchmaking.util;

public final class Constants {
    public static final int SCENE_WIDTH = 800;
    public static final int SCENE_HEIGHT = 450;

    public static final int PLAYER_SIZE = 50;
    public static final float PLAYER_SPEED = 200;
    public static final int PLAYERS_PER_GAME = 5;

    public static final int PACKET_HEADER_LENGTH = 5;
    public static final int PACKET_LENGTH = 256;
    public static final int REQUEST_ATTEMPTS = 3;
    public static final int REQUEST_ATTEMPT_INTERVAL = 3000;
    public static final int GAME_UPDATE_INTERVAL = 50;

    public static final int QUEUE_TIMEOUT = 60000;
    public static final int GAME_LENGTH = 30000;

    public static final ProtoSocket.Location MATCHMAKING_SERVER = new ProtoSocket.Location(
            "127.0.0.1",
            3000
    );
}