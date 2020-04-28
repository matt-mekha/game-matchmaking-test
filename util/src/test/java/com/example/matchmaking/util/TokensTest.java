package com.example.matchmaking.util;

import com.google.protobuf.ByteString;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class TokensTest {

    @Test
    public void testEncoding() {
        ByteString token = Tokens.generate();
        assertArrayEquals(Tokens.decode(Tokens.encode(token)).toByteArray(), token.toByteArray());
    }

    @Test
    public void byteStringEquals() {
        ByteString token = Tokens.generate();
        ByteString token2 = ByteString.copyFrom(token.toByteArray());
        assertEquals(token, token2);
    }

}
