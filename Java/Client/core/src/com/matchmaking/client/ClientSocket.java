package com.matchmaking.client;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.matchmaking.client.RequestProtos.*;

public class ClientSocket {

    private final static int MATCHMAKING_SERVER_PORT = 3000;
    private final String matchmakingAddressName;

    private final static int SOCKET_TIMEOUT = 5000;
    private final static int QUEUE_TIMEOUT = 30000;
    private final static int MAX_TRIES = 3;
    private final static int PROTO_PACKET_SIZE = 256;
    private final static int REQUESTS_PER_SECOND = 30;
    private final static int REQUEST_SLEEP_TIME = 1000 / REQUESTS_PER_SECOND;

    private final Game game;
    private DatagramSocket socket;
    private List<RequestProtos.Player> players = new ArrayList<>();

    private Status status = Status.CONNECTING;

    public ClientSocket(Game game, String matchmakingAddressName) {
        this.game = game;
        this.matchmakingAddressName = matchmakingAddressName;
        try {
            socket = new DatagramSocket();
            socket.setSoTimeout(SOCKET_TIMEOUT);
            Thread thread = new Thread(){
                @Override
                public void run() {
                    communicate();
                }
            };
            thread.start();
        } catch (SocketException e) {
            fatalException(e);
        }
    }

    private void fatalException(Exception e) {
        e.printStackTrace();
        Logger.getInstance().log(e.toString());
        Logger.getInstance().log(Arrays.toString(e.getStackTrace()));
        close();
        Gdx.app.exit();
    }

    private void send(byte[] bytes, InetAddress address, int port) {
        try {
            DatagramPacket request = new DatagramPacket(bytes, bytes.length, address, port);
            socket.send(request);
        } catch (IOException e) {
            fatalException(e);
        }
    }

    private byte[] receive(int length, InetAddress address, int port) throws SocketTimeoutException {
        byte[] buffer = new byte[length];
        DatagramPacket response = new DatagramPacket(buffer, length);
        try {
            socket.receive(response);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
            throw e;
        } catch (IOException e) {
            fatalException(e);
        }
        return buffer;
    }

    private byte[] request(byte[] requestBytes, int responseLength, InetAddress address, int port) throws IOException {
        for (int i = 0; i < MAX_TRIES; i++) {
            send(requestBytes, address, port);
            try {
                return receive(responseLength, address, port);
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
            }
        }
        throw new IOException("Failed to receive a response from server after multiple attempts.");
    }

    private Response request(RequestType requestType, InetAddress address, int port) throws IOException {
        byte[] requestProtoBytes = Request.newBuilder().setPlayer(game.getMyPlayer()).setType(requestType).build().toByteArray();

        ByteBuffer requestBuffer = ByteBuffer.allocate(PROTO_PACKET_SIZE);
        requestBuffer.putInt(requestProtoBytes.length);
        requestBuffer.put(requestProtoBytes);

        byte[] responseBytes = request(requestBuffer.array(), PROTO_PACKET_SIZE, address, port);
        ByteBuffer responseBuffer = ByteBuffer.wrap(responseBytes);
        int dataLength = responseBuffer.getInt();

        return Response.parseFrom(Arrays.copyOfRange(responseBytes, 4, 4 + dataLength));
    }

    private void checkFirstByte(byte[] response, int expected) throws IOException {
        byte expectedByte = (byte) expected;
        if(response[0] != expectedByte) {
            throw new IOException(String.format("Malformed server response. Expected %d but got %d", expectedByte, response[0]));
        }
    }

    private void checkSuccess(Response responseProto) throws IOException {
        if(!responseProto.getSuccess()) {
            throw new IOException("Server response unsuccessful.");
        }
    }

    private void communicate() {
        try {

            // MATCHMAKING

            InetAddress matchmakingAddress = InetAddress.getByName(matchmakingAddressName);

            byte[] queueResponse = request(new byte[]{0x01}, 1, matchmakingAddress, MATCHMAKING_SERVER_PORT);

            checkFirstByte(queueResponse, 0x01);
            status = Status.QUEUED;

            socket.setSoTimeout(QUEUE_TIMEOUT);
            byte[] matchResponse = receive(9, matchmakingAddress, MATCHMAKING_SERVER_PORT);
            socket.setSoTimeout(SOCKET_TIMEOUT);
            checkFirstByte(matchResponse, 0x02);

            status = Status.JOINING;

            InetAddress gameAddress = InetAddress.getByAddress(Arrays.copyOfRange(matchResponse, 1, 5));
            int gamePort = ByteBuffer.wrap(Arrays.copyOfRange(matchResponse, 5, 9)).getInt();

            // GAME

            Response responseProto = request(RequestType.JOIN, gameAddress, gamePort);
            checkSuccess(responseProto);
            status = Status.GAME;

            while (true) {
                responseProto = request(RequestType.GET, gameAddress, gamePort);
                checkSuccess(responseProto);

                if(responseProto.getType() == RequestType.LEAVE) {
                    break;
                }

                players = responseProto.getPlayersList();
                Thread.sleep(REQUEST_SLEEP_TIME);
            }

            Gdx.app.exit();

        } catch (InterruptedException | IOException e) {
            fatalException(e);
        }
    }

    public void close() {
        if(socket != null && !socket.isClosed()) {
            socket.close();
        }
        Logger.getInstance().close();
    }

    public List<RequestProtos.Player> getPlayers() {
        return players;
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        CONNECTING,
        QUEUED,
        JOINING,
        GAME;

        private String info = "";

        public String getInfo() {
            return (info.equals("")) ? toString() : info;
        }

        public void setInfo(String info) {
            this.info = info;
        }
    }
}
