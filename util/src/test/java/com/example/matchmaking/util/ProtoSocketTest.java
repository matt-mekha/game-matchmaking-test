package com.example.matchmaking.util;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProtoSocketTest {

    @Test
    public void sameAddress() {
        assertEquals(
                new ProtoSocket.Location("127.0.0.1", 3000),
                new ProtoSocket.Location("127.0.0.1", 3000)
        );
    }

}
