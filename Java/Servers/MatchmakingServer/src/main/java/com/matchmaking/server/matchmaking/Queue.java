package com.matchmaking.server.matchmaking;

import java.net.InetAddress;
import java.util.ArrayList;

public class Queue {

    private final static int GAME_SIZE = 5;

    private final ServerSocket socket;
    private final ArrayList<Client> clients = new ArrayList<>();

    public Queue(ServerSocket socket) {
        this.socket = socket;
    }

    public void add(InetAddress address, int port) {
        synchronized (clients) {
            clients.add(new Client(address, port));
            if(clients.size() >= GAME_SIZE) {
                ArrayList<Client> nextMatchClients = new ArrayList<>(GAME_SIZE);
                for (int i = 0; i < GAME_SIZE; i++) {
                    nextMatchClients.add(clients.remove(0));
                }
                socket.startMatch(nextMatchClients);
            }
        }
    }

}
