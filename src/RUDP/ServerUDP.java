package RUDP;

import OBJS.ChannelThread;
import OBJS.Connection;
import OBJS.RecvBuffer;
import OBJS.ServerConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ServerUDP {
    private static final Logger logger = LoggerFactory.getLogger(ServerUDP.class);


    private DatagramChannel channel;
    private Connection connection;
    private ChannelThread thread;
    private RecvBuffer recvBuffer;
    private SocketAddress localAddr;
    private SocketAddress routerAddr;

    public ServerUDP(int localPort) throws IOException {
        this.localAddr = new InetSocketAddress( localPort);
        this.routerAddr = new InetSocketAddress("localhost", 3000);
        this.init();
    }

    private void init() throws IOException {

        this.channel = DatagramChannel.open();
        this.channel.bind(this.localAddr);
        logger.info("Server is listening on: " + this.localAddr);

        this.thread = new ChannelThread(this.channel, this.routerAddr);
        Thread recvThread = new Thread(this.thread);
        this.connection = new ServerConnection(this.thread, this.routerAddr);
        this.thread.subscribe(this.connection);
        this.thread.bind(this.connection);

        // allocate the data received buffer
        this.recvBuffer = new RecvBuffer(this.thread, this.routerAddr, this.connection);
        this.thread.subscribe(this.recvBuffer);
        recvThread.start();
    }

    public void send(String message) throws IOException {
        this.thread.send(message.getBytes("utf-8"));
    }

    public int receive(ByteBuffer result) {
        return this.recvBuffer.receive(result);
    }
}