package com.matchmaking.server.game;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;

public class ServerSocket {

    private final static String MATCHMAKING_SERVER_IP = "127.0.0.1";
    private final static int MATCHMAKING_SERVER_PORT = 3000;
    private final static int GAME_SERVER_START_PORT = 3001;
    private final static int GAME_SERVER_END_PORT = 4000;

    private final static int GAME_LENGTH = 30000;
    private final static int LEAVE_LENGTH = 5000;
    private final static int PACKET_SIZE = 256;

    private boolean gameOver = false;

    private DatagramSocket socket;

    private final HashMap<String, RequestProtos.Player> clientProtos = new HashMap<>();

    public ServerSocket(String token) {
        try {

            socket = getNextAvailableSocket();
            socket.setSoTimeout(GAME_LENGTH);

            ByteBuffer buffer = ByteBuffer.allocate(33);
            buffer.put((byte)0x03);
            buffer.put(Base64.getUrlDecoder().decode(token));

            Logger.getInstance().log("sending token " + token + " to matchmaking server");
            send(buffer.array(), InetAddress.getByName(MATCHMAKING_SERVER_IP), MATCHMAKING_SERVER_PORT);

            Logger.getInstance().log("listening for players...");

            long endTime = System.currentTimeMillis() + GAME_LENGTH;
            while(System.currentTimeMillis() < endTime) {
                DatagramPacket packet = receive(false);
                Thread responseThread = new Thread(() -> respond(packet));
                responseThread.start();
            }

            gameOver = true;
            socket.setSoTimeout(LEAVE_LENGTH);

            Logger.getInstance().log("telling players to leave...");

            try {
                endTime = System.currentTimeMillis() + LEAVE_LENGTH;
                while (System.currentTimeMillis() < endTime) {
                    DatagramPacket packet = receive(true);
                    Thread responseThread = new Thread(() -> respond(packet));
                    responseThread.start();
                }
            } finally {
                Logger.getInstance().log("game finished successfully");
                close();
            }
            System.exit(0);

        } catch (IOException e) {
            fatalException(e);
        }
    }

    private DatagramSocket getNextAvailableSocket() {
        for(int port = GAME_SERVER_START_PORT; port <= GAME_SERVER_END_PORT; port++) {
            boolean success = false;
            try {
                socket = new DatagramSocket(port);
                success = true;
            } catch(IOException e) {
                if(socket != null){
                    socket.close();
                    socket = null;
                }
            }
            if(success) {
                return socket;
            }
        }
        return null;
    }

    private void send(RequestProtos.Response response, InetAddress address, int port) {
        byte[] responseProtoBytes = response.toByteArray();

        ByteBuffer buffer = ByteBuffer.allocate(PACKET_SIZE);
        buffer.putInt(responseProtoBytes.length);
        buffer.put(responseProtoBytes);

        send(buffer.array(), address, port);
    }

    private void send(byte[] bytes, InetAddress address, int port) {
        try {
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, address, port);
            socket.send(request);
        } catch (IOException e) {
            fatalException(e);
        }
    }

    private DatagramPacket receive(boolean timeoutOK) throws IOException {
        byte[] buffer = new byte[PACKET_SIZE];
        DatagramPacket response = new DatagramPacket(buffer, PACKET_SIZE);
        try {
            socket.receive(response);
        } catch (IOException e) {
            if(timeoutOK && e instanceof SocketTimeoutException) {
                throw e;
            } else {
                fatalException(e);
            }
        }
        return response;
    }

    private void respond(DatagramPacket packet) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
            int dataLength = buffer.getInt();
            RequestProtos.Request request = RequestProtos.Request.parseFrom(Arrays.copyOfRange(packet.getData(), 4, 4 + dataLength));
            RequestProtos.Response response;

            Client client = new Client(packet.getAddress(), packet.getPort());

            Logger.getInstance().log(String.format("received %s request from %s",
                    request.getType().toString(),
                    client.toString()
            ));

            if(gameOver) {
                Logger.getInstance().log("telling them to leave...");
                response = RequestProtos.Response.newBuilder()
                        .setType(RequestProtos.RequestType.LEAVE)
                        .setSuccess(true)
                        .build();
                send(response, packet.getAddress(), packet.getPort());
                return;
            }

            switch(request.getType()) {
                case JOIN:

                    synchronized (clientProtos) { clientProtos.put(client.toString(), request.getPlayer()); }

                    response = RequestProtos.Response.newBuilder()
                            .setType(RequestProtos.RequestType.JOIN)
                            .setSuccess(true)
                            .build();
                    send(response, packet.getAddress(), packet.getPort());

                    break;
                case GET:

                    synchronized (clientProtos) {
                        clientProtos.put(client.toString(), request.getPlayer());
                    }

                    response = RequestProtos.Response.newBuilder()
                            .addAllPlayers(clientProtos.values())
                            .setType(RequestProtos.RequestType.GET)
                            .setSuccess(true)
                            .build();
                    send(response, packet.getAddress(), packet.getPort());

                    break;
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    private void fatalException(Exception e) {
        e.printStackTrace();
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
