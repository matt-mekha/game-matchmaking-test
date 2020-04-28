package com.example.matchmaking.gameserver;

import com.example.matchmaking.util.Logger;

public class Main {
    public static void main(String[] args) {
        System.out.println("hello");
        Logger.setName("GameServer");
        new GameServer(args);
    }
}
