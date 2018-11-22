package OBJS;

import RUDP.Packet;
import manager.NoticeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServerConnection extends Connection {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnection.class);

    private long remoteSeqNum;
    private long localSeqNum;

    private SocketAddress router;
    private ChannelThread channelThread;
    private InetSocketAddress targetAddress;
    private boolean connected;

    public ServerConnection(ChannelThread thread, SocketAddress routerAddress) {
        super();
        this.channelThread = thread;
        this.router = routerAddress;
        this.localSeqNum = 1000;
        this.targetAddress = new InetSocketAddress("localhost", 8098);
    }

    protected void update(NoticeMsg msg, Packet packet) throws IOException {
        switch (msg) {
            case SYN:
                this.answerSYN(packet);
                this.answerSYNACK(packet);
                break;
            case SYN_ACK:
                if (packet.getSequenceNumber() == this.localSeqNum + 1) {
                    logger.info("Handshaking #3 SYN packet has received");
                    this.localSeqNum = packet.getSequenceNumber();
                    this.connected = true;
                    break;
                }
        }
    }

    private void answerSYN(Packet recvPacket) throws IOException {
        logger.info("Handshaking #1 SYN packet has received");
        this.remoteSeqNum = recvPacket.getSequenceNumber();
        Packet p = new Packet.Builder()
                .setType(Packet.SYN_1)
                .setSequenceNumber(this.localSeqNum)
                .setPortNumber(this.targetAddress.getPort())
                .setPeerAddress(this.targetAddress.getAddress())
                .setPayload("hi".getBytes())
                .create();
//        this.channelThread.getChannel().send(p.toBuffer(), this.router);
        this.channelThread.getChannel().send(p.toBuffer(), this.router);

        logger.info("Handshaking #2 SYN packet has sent out");
    }

    private void answerSYNACK(Packet recvPacket) throws IOException {
        Packet p = new Packet.Builder()
                .setType(Packet.SYN_2)
                .setSequenceNumber(this.remoteSeqNum + 1)
                .setPortNumber(this.targetAddress.getPort())
                .setPeerAddress(this.targetAddress.getAddress())
                .setPayload("".getBytes())
                .create();

//            this.channelThread.getChannel().send(p.toBuffer(), this.router);
        this.channelThread.getChannel().send(p.toBuffer(), this.router);

        logger.info("Handshaking #2 SYN_ACK packet has sent out");
    }
}
