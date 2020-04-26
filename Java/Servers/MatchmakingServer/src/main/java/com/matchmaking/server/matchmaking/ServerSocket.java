package com.matchmaking.server.matchmaking;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class ServerSocket {

    private final static int PACKET_SIZE = 33;

    private final static int PORT = 3000;

    private final static byte QUEUE = 0x01;
    private final static byte MATCH = 0x02;
    private final static byte TOKEN = 0x03;
    private final static byte ERROR = 0x0F;

    private DatagramSocket socket;
    private Queue queue;
    private final ArrayList<GameServer> gameServers = new ArrayList<>();

    public ServerSocket() {
        try {
            socket = new DatagramSocket(PORT);
            queue = new Queue(this);

            Logger.getInstance().log("UP AND RUNNING!");

            while(true) {
                DatagramPacket packet = receive();
                Thread responseThread = new Thread(() -> respond(packet));
                responseThread.start();
            }
        } catch (SocketException e) {
            fatalException(e);
        }
    }

    public void startMatch(ArrayList<Client> clients) {
        try {

            Logger.getInstance().log("spawning game server...");

            GameServer gameServer = new GameServer();
            gameServers.add(gameServer);

            ByteBuffer byteBuffer = ByteBuffer.allocate(9);
            byteBuffer.put(MATCH);
            byteBuffer.put(gameServer.getAddress().getAddress());
            byteBuffer.putInt(gameServer.getPort());
            byte[] matchFoundBytes = byteBuffer.array();

            Logger.getInstance().log("successfully spawned game server");
            for(Client client : clients) {
                send(matchFoundBytes, client.getAddress(), client.getPort());
            }

        } catch (IOException | InterruptedException e) {
            fatalException(e);
        }
    }

    private void respond(DatagramPacket packet) {
        byte requestType = packet.getData()[0];
        InetAddress address = packet.getAddress();
        int port = packet.getPort();

        boolean isServer = false;
        for(GameServer gameServer : gameServers) {
            if(gameServer.getAddress() == address && gameServer.getPort() == port) {
                isServer = true;
                break;
            }
        }

        Logger.getInstance().log(String.format("received request from %s:%d", packet.getAddress(), packet.getPort()));

        if (isServer) {

        } else {
            if (requestType == QUEUE) {
                send(new byte[]{QUEUE}, address, port);
                queue.add(address, port);
            } else if (requestType == TOKEN) {
                GameServer.confirmToken(Arrays.copyOfRange(packet.getData(), 1, 33), address, port);
            } else {
                send(new byte[]{ERROR}, address, port);
            }
        }
    }

    private void send(byte[] bytes, InetAddress address, int port) {
        try {
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, address, port);
            socket.send(request);
        } catch (IOException e) {
            fatalException(e);
        }
    }

    private DatagramPacket receive() {
        byte[] buffer = new byte[PACKET_SIZE];
        DatagramPacket response = new DatagramPacket(buffer, PACKET_SIZE);
        try {
            socket.receive(response);
        } catch (IOException e) {
            fatalException(e);
        }
        return response;
    }

    private void fatalException(Exception e) {
        e.printStackTrace();
        Logger.getInstance().log(e.toString());
        close();
        System.exit(-1);
    }

    private void close() {
        if(socket != null && !socket.isClosed()) {
            socket.close();
        }
        Logger.getInstance().close();
    }

}
