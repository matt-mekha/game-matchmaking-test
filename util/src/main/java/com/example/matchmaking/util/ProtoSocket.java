package com.example.matchmaking.util;

import static com.example.matchmaking.util.ConnectionProtos.*;
import com.google.protobuf.*;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Function;

public class ProtoSocket {

    private final LinkedBlockingQueue<ProtoPacket<? extends Message>> packetQueue = new LinkedBlockingQueue<>();

    private final HashMap<MessageType, PacketConsumer> activeRequestCallbacks = new HashMap<>();
    private final HashMap<MessageType, PacketResponder> activeResponseCallbacks = new HashMap<>();

    private DatagramSocket socket;

    public ProtoSocket() {
        try {
            socket = new DatagramSocket();
        } catch(IOException e) {
            if(socket != null) {
                socket.close();
            }
            Logger.fatalException(e);
        }
        Logger.logf("opened socket on port %d", socket.getLocalPort());
        beginThreads();
    }

    public ProtoSocket(int port) throws IOException {
        this(port, port);
    }

    public ProtoSocket(int startPort, int endPort) throws IOException {
        for(int port = startPort; port <= endPort; port++) {
            try {
                socket = new DatagramSocket(port);
                break;
            } catch(IOException ignored) {
                if(socket != null) {
                    socket.close();
                }
            }
        }
        if(socket == null) {
            throw new IOException(String.format("no available ports between %d and %d", startPort, endPort));
        }
        Logger.logf("opened socket on port %d", socket.getLocalPort());
        beginThreads();
    }

    /**
     * Sends a request expecting a response with the same MessageType field. Only one active request of each message
     * type is allowed.
     * @param protoPacket the ProtoPacket to be sent
     * @param callback a consumer that accepts a Response
     */
    public void request(ProtoPacket<Request> protoPacket, Consumer<Response> callback) {
        Logger.logf(
                "sending %s request to %s",
                protoPacket.getMessageType().toString(),
                protoPacket.getLocation().toString()
        );

        PacketConsumer packetConsumer = new PacketConsumer(protoPacket, callback);
        activeRequestCallbacks.put(protoPacket.getMessageType(), packetConsumer);
        new Thread(() -> {
            try {
                int attempts = 0;
                while(attempts < Constants.REQUEST_ATTEMPTS && packetConsumer.isWaiting()) {
                    if(attempts > 0)
                        Logger.logf("request timed out - trying again", protoPacket.getLocation().toString());
                    send(protoPacket);
                    attempts++;
                    Thread.sleep(Constants.REQUEST_ATTEMPT_INTERVAL);
                }
                if(packetConsumer.isWaiting())
                    throw new IOException("failed to receive response after multiple requests");
            } catch (InterruptedException | IOException e) {
                Logger.fatalException(e);
            }
        }).start();
    }

    /**
     * Alias for <code>listen()</code> without a timeout.
     * @param messageType the type of message
     * @param callback a Function that accepts a Request and returns a Response
     */
    public void listen(MessageType messageType, Function<ProtoPacket<Request>, Response> callback) {
        listen(messageType, callback, null, -1);
    }

    /**
     * Listens for Request packets and calls the callback function to determine what to respond.
     * @param messageType the type of message
     * @param callback a function that accepts a Request and returns a Response
     * @param onFinish a Consumer that accepts <code>true</code> if nothing was received and <code>false</code> if
     *                 otherwise
     * @param timeout most milliseconds to wait for response
     */
    public void listen(MessageType messageType, Function<ProtoPacket<Request>, Response> callback, Consumer<Boolean> onFinish, int timeout) {
        Logger.logf("listening for %s requests", messageType.toString());

        PacketResponder packetResponder = new PacketResponder(callback);
        activeResponseCallbacks.put(messageType, packetResponder);
        if(timeout > 0) {
            new Thread(() -> {
                try {
                    Thread.sleep(timeout);
                    onFinish.accept(packetResponder.isWaiting());
                } catch (InterruptedException e) {
                    Logger.fatalException(e);
                }
            }).start();
        }
    }

    private void send(ProtoPacket<? extends Message> protoPacket) {
        byte[] protoBytes = protoPacket.getMessage().toByteArray();
        ByteBuffer byteBuffer = ByteBuffer.allocate(Constants.PACKET_HEADER_LENGTH + protoBytes.length);
        byteBuffer.put((byte) (protoPacket.getPacketType() == Request.class ? 0x0 : 0x1));
        byteBuffer.putInt(protoBytes.length);
        byteBuffer.put(protoBytes);
        byte[] bytes = byteBuffer.array();

        DatagramPacket packet = new DatagramPacket(
                bytes,
                bytes.length,
                protoPacket.getLocation().getAddress(),
                protoPacket.getLocation().getPort()
        );

        try {
            socket.send(packet);
        } catch (IOException e) {
            Logger.fatalException(e);
        }
    }

    private void beginThreads() {
        new Thread(this::receiveForever).start();
        new Thread(this::respondForever).start();
    }

    private void receiveForever() {
        while(true) {
            byte[] buffer = new byte[Constants.PACKET_LENGTH];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(packet);
                packetQueue.add(new ProtoPacket<>(packet));
            } catch (IOException e) {
                Logger.fatalException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void respondForever() {
        try {
            while(true) {
                ProtoPacket<? extends Message> packet = packetQueue.take();
                MessageType messageType = packet.getMessageType();

                if(packet.getPacketType() == Request.class) {
                    ProtoPacket<Request> requestPacket = (ProtoPacket<Request>) packet;
                    Logger.logf(
                            "received %s request from %s",
                            requestPacket.getMessageType(),
                            requestPacket.getLocation().toString()
                    );

                    PacketResponder responseCallback = activeResponseCallbacks.get(messageType);
                    if(responseCallback == null) {
                        Logger.logf("the %s request has no registered callback", requestPacket.getMessageType());
                    } else {
                        Logger.log("sending response");
                        Response responseProto = responseCallback.apply(requestPacket);
                        send(new ProtoPacket<>(
                                responseProto,
                                packet.getLocation()
                        ));
                    }
                } else {
                    ProtoPacket<Response> responsePacket = (ProtoPacket<Response>) packet;
                    Logger.logf(
                            "received %s response from %s",
                            responsePacket.getMessageType(),
                            responsePacket.getLocation().toString()
                    );

                    PacketConsumer packetConsumer = activeRequestCallbacks.remove(messageType);
                    if(packetConsumer == null) {
                        Logger.logf("the %s response has no registered callback", responsePacket.getMessageType());
                    } else {
                        if (packetConsumer.getSentPacket().matches(responsePacket)) {
                            assert responsePacket.getMessage().getSuccess();
                            packetConsumer.accept(responsePacket.getMessage());
                        } else {
                            Logger.logf("the %s response is from a different source - ignoring", responsePacket.getMessageType());
                        }
                    }
                }


            }
        } catch (InterruptedException e) {
            Logger.fatalException(e);
        }
    }

    public static class Location {
        private InetAddress address;
        private final int port;

        public Location(DatagramPacket packet) {
            this.address = packet.getAddress();
            this.port = packet.getPort();
        }

        public Location(String addressName, int port) {
            try {
                this.address = InetAddress.getByName(addressName);
            } catch (UnknownHostException e) {
                Logger.fatalException(e);
            }
            this.port = port;
        }

        public Location(Access access) {
            try {
                this.address = InetAddress.getByAddress(access.getAddress().toByteArray());
            } catch (UnknownHostException e) {
                Logger.fatalException(e);
            }
            this.port = access.getPort();
        }

        public Access toProto() {
            return Access.newBuilder()
                    .setAddress(ByteString.copyFrom(address.getAddress()))
                    .setPort(port)
                    .buildPartial();
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public void setAddress(InetAddress address) {
            this.address = address;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return port == location.port && address.equals(location.getAddress());
        }

        @Override
        public String toString() {
            return String.format("%s:%d", address.toString(), port);
        }
    }

    public static class ProtoPacket<T extends Message> {
        private final Location location;
        private final T message;
        private final Class<? extends  Message> packetType;

        private static final HashMap<Integer, Class<? extends Message>> packetTypes = new HashMap<>();
        private static final HashMap<Class<? extends Message>, Parser<? extends Message>> parsers = new HashMap<>();

        static {
            packetTypes.put(0x0, Request.class);
            packetTypes.put(0x1, Response.class);

            parsers.put(Request.class, Request.parser());
            parsers.put(Response.class, Response.parser());
        }

        @SuppressWarnings("unchecked")
        private ProtoPacket(DatagramPacket packet) throws InvalidProtocolBufferException {
            ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData());
            this.packetType = packetTypes.get((int)byteBuffer.get());
            int protoLength = byteBuffer.getInt();
            byte[] protoBytes = protoLength > 0
                    ? Arrays.copyOfRange(
                            packet.getData(),
                            Constants.PACKET_HEADER_LENGTH,
                            Constants.PACKET_HEADER_LENGTH + protoLength
                    )
                    : new byte[0];

            this.message = (T) parsers.get(packetType).parseFrom(protoBytes);
            this.location = new Location(packet);
        }

        public ProtoPacket(T message, Location location) {
            this.packetType = message.getClass();
            this.message = message;
            this.location = location;
        }

        public Location getLocation() {
            return location;
        }

        public T getMessage() {
            return message;
        }

        public MessageType getMessageType() {
            if(packetType == Request.class) {
                Request requestMessage = (Request) message;
                return requestMessage.getType();
            } else {
                Response responseMessage = (Response) message;
                return responseMessage.getType();
            }
        }

        public Class<? extends Message> getPacketType() {
            return packetType;
        }

        public boolean matches(ProtoPacket<? extends Message> otherPacket) {
            return this.location.equals(otherPacket.location);
        }
    }

    private static class PacketConsumer {
        private final ProtoPacket<Request> sentPacket;
        private final Consumer<Response> consumer;

        private boolean waiting = true;

        public PacketConsumer(ProtoPacket<Request> sentPacket, Consumer<Response> consumer) {
            this.sentPacket = sentPacket;
            this.consumer = consumer;
        }

        public void accept(Response t) {
            if(!waiting) return;
            waiting = false;
            consumer.accept(t);
        }

        public boolean isWaiting() {
            return waiting;
        }

        public ProtoPacket<Request> getSentPacket() {
            return sentPacket;
        }
    }

    private static class PacketResponder {
        private final Function<ProtoPacket<Request>, Response> responder;
        private boolean waiting = true;

        public PacketResponder(Function<ProtoPacket<Request>, Response> responder) {
            this.responder = responder;
        }

        public Response apply(ProtoPacket<Request> t) {
            waiting = false;
            return responder.apply(t);
        }

        public boolean isWaiting() {
            return waiting;
        }
    }

}
