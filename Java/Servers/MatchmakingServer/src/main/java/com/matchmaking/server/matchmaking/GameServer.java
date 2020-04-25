package com.matchmaking.server.matchmaking;

import java.io.IOException;
import java.net.InetAddress;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;


public class GameServer {

    private final static String GAME_SERVER_JAR_NAME = "GameServer";
    private final static int TOKEN_LENGTH = 32;
    private final static int SPAWN_SERVER_TIMEOUT = 5000;
    private final static int SPAWN_SERVER_WAIT_INTERVAL = 20;

    private final static SecureRandom secureRandom = new SecureRandom();
    private final static Base64.Encoder base64Encoder = Base64.getUrlEncoder();
    private final static ArrayList<String> tokens = new ArrayList<>();
    private final static HashMap<String, GameServer> servers = new HashMap<>();

    private boolean confirmed = false;
    private InetAddress address;
    private int port;

    public GameServer() throws IOException, InterruptedException {
        String token = generateToken();
        servers.put(token, this);
        tokens.add(token);
        Process process = Runtime.getRuntime().exec(String.format("java -jar %s.jar %s", GAME_SERVER_JAR_NAME, token));
        StreamPipe.pipe(process.getErrorStream(), System.err);
        StreamPipe.pipe(process.getInputStream(), System.out);

        int totalWaitTime = 0;
        while(!confirmed) {
            Thread.sleep(SPAWN_SERVER_WAIT_INTERVAL);
            totalWaitTime += SPAWN_SERVER_WAIT_INTERVAL;
            if (totalWaitTime >= SPAWN_SERVER_TIMEOUT) {
                throw new IOException("Failed to spawn server.");
            }
        }
    }

    private void confirm(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        this.confirmed = true;
    }

    public static void confirmToken(byte[] tokenBytes, InetAddress address, int port) {
        String token = base64Encoder.encodeToString(tokenBytes);
        if(tokens.remove(token)) {
            servers.get(token).confirm(address, port);
            servers.remove(token);
        }
    }

    private String generateToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }
}
