package com.example.matchmaking.matchmaker;

import com.example.matchmaking.util.Logger;

public class Main {
    public static void main(String[] args) {
        Logger.setName("Matchmaker");
        new MatchmakingServer();
    }
}
