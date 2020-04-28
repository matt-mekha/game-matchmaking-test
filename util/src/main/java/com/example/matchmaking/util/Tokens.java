package com.example.matchmaking.util;

import com.google.protobuf.ByteString;

import java.security.SecureRandom;

public class Tokens {

    private static final SecureRandom secureRandom = new SecureRandom();

    public static ByteString generate() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return ByteString.copyFrom(tokenBytes);
    }

}
