package com.matchmaking.server.matchmaking;

import java.net.InetAddress;
import java.util.ArrayList;

public class Queue {

    private final static int GAME_SIZE = 5;

    private final ArrayList<Player> players = new ArrayList<>();

    public boolean add(InetAddress address, int port) {
        synchronized (players) {
            players.add(new Player(address, port));
            return players.size() >= GAME_SIZE;
        }
    }

    public ArrayList<Player> getNextMatchPlayers() {
        synchronized (players) {
            ArrayList<Player> nextMatchPlayers = new ArrayList<>(GAME_SIZE);
            for (int i = 0; i < GAME_SIZE; i++) {
                nextMatchPlayers.add(players.remove(0));
            }
            return nextMatchPlayers;
        }
    }

}
