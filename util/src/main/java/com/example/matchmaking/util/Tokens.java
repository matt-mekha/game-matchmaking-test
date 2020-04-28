package com.example.matchmaking.util;

import com.google.protobuf.ByteString;

import java.security.SecureRandom;
import java.util.Base64;

public class Tokens {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();
    private static final Base64.Decoder decoder = Base64.getUrlDecoder();

    public static ByteString generate() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return ByteString.copyFrom(tokenBytes);
    }

    public static String encode(ByteString bytes) {
        return encoder.encodeToString(bytes.toByteArray());
    }

    public static ByteString decode(String string) {
        return ByteString.copyFrom(decoder.decode(string));
    }

}
