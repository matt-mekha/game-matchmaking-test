package com.example.matchmaking.matchmaker;

import com.example.matchmaking.util.ProtoSocket;

import java.util.ArrayList;

public class ClientQueue {

    private final ArrayList<ProtoSocket.Location> list = new ArrayList<>();
    private final int size;

    public ClientQueue(int size) {
        this.size = size;
    }

    public boolean add(ProtoSocket.Location item) {
        synchronized (list) {
            list.add(item);
            return list.size() >= size;
        }
    }

    public ArrayList<ProtoSocket.Location> get() {
        final ArrayList<ProtoSocket.Location> result = new ArrayList<>();
        synchronized (list) {
            if(list.size() < size) {
                throw new IllegalStateException("queue does not have that many items");
            }
            for (int i = 0; i < size; i++) {
                result.add(list.remove(0));
            }
        }
        return result;
    }

}
