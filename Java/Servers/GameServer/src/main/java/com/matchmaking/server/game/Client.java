package com.matchmaking.server.game;

import java.net.InetAddress;
import java.util.Objects;

public class Client {

    private final InetAddress address;
    private final int port;

    public Client(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public boolean equals(InetAddress address, int port) {
        return (this.address.getAddress() == address.getAddress()) && (this.port == port);
    }

    @Override
    public boolean equals(Object object) {
        try {
            Client otherClient = (Client) object;
            return this.equals(otherClient.address, otherClient.port);
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(address.getAddress(), port);
    }

    @Override
    public String toString() {
        return String.format("%s:%d", address.toString(), port);
    }
}
