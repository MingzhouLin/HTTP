package RUDP;

import OBJS.ChannelThread;
import OBJS.Connection;
import OBJS.RecvBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

public class ClientUDP {
    private static final Logger logger = LoggerFactory.getLogger(ClientUDP.class);

    private DatagramChannel channel;
    private Connection connection;
    private ChannelThread thread;
    private SocketAddress localAddr;
    private SocketAddress   routerAddr;
    private RecvBuffer recvBuffer;

    public ClientUDP(int localPort, int serverPort) throws IOException {
        this.localAddr = new InetSocketAddress(localPort);
        this.routerAddr = new InetSocketAddress("localhost", 3000);
        this.init(serverPort);
    }

    public void init(int serverPort) throws IOException {
        channel=DatagramChannel.open();
        logger.info("Client is binding to: " + this.localAddr);
        this.channel.bind(this.localAddr);

        thread=new ChannelThread(channel, this.routerAddr);
        connection=new Connection(thread, routerAddr);
        thread.subscribe(connection);
        thread.bind(connection);

        this.recvBuffer = new RecvBuffer(this.thread, this.routerAddr, this.connection);
        this.thread.subscribe(this.recvBuffer);

        connection.connect(new InetSocketAddress("localhost", serverPort));
        new Thread(this.thread).start();
    }

    public void send(String message) throws IOException {
        this.thread.send(message.getBytes("utf-8"));
    }

    public int receive(ByteBuffer result) {
        return this.recvBuffer.receive(result);
    }
}
