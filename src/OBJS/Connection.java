package OBJS;

import RUDP.Packet;
import manager.Manager;
import manager.NoticeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;

public class Connection extends Manager {
    private static final Logger logger = LoggerFactory.getLogger(Connection.class);

    private long localSeqNum;
    private long remoteSeqNum;
    private InetSocketAddress targetAddress;
    private DatagramChannel channel;
    private SocketAddress routerAddress;
    private ChannelThread thread;
    private boolean connected;

    public Connection() {
    }

    public Connection(ChannelThread thread, SocketAddress routerAddress) {
        this.thread = thread;
        this.routerAddress = routerAddress;
        this.localSeqNum = 1000;
    }

    public void connect(InetSocketAddress targetAddress)
            throws IOException {
        this.targetAddress = targetAddress;
        this.sendInitialSegment();
    }

    public void sendInitialSegment() throws IOException {
        Packet packet = new Packet.Builder()
                .setType(Packet.SYN_1)
                .setSequenceNumber(this.localSeqNum)
                .setPortNumber(this.targetAddress.getPort())
                .setPeerAddress(this.targetAddress.getAddress())
                .setPayload("Hi".getBytes())
                .create();

        this.thread.getChannel().send(packet.toBuffer(), this.routerAddress);
        logger.info("Handshaking #1 SYN packet has already sent out");
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    protected void update(NoticeMsg msg, Packet packet) throws IOException {
        switch (msg) {
            case SYN_ACK:
                if (this.localSeqNum + 1 == packet.getSequenceNumber()) {
                    logger.info("Handshaking #2 ACK_SYN packet has received");
                    this.connected = true;
                    logger.info("Handshaking success, connection established");
                }
                break;
            case SYN:
                this.remoteSeqNum = packet.getSequenceNumber();
                Packet p = new Packet.Builder()
                        .setType(Packet.SYN_2)
                        .setSequenceNumber((this.remoteSeqNum + 1))
                        .setPortNumber(this.targetAddress.getPort())
                        .setPeerAddress(this.targetAddress.getAddress())
                        .setPayload("".getBytes())
                        .create();

//                this.channelThread.getChannel().send(p.toBuffer(), this.router);
                this.thread.getChannel().send(p.toBuffer(), this.routerAddress);
                logger.info("Handshaking #3 ACK_SYN packet has sent out");
        }
    }

    public Packet[] makeChunks(byte[] message) {
        int mLen = message.length;
        int packetAmt = (mLen / Packet.MAX_DATA) + 1;
        int offset = 0;

        Packet[] packets = new Packet[packetAmt + 1];
        for (int i = 0; i < packets.length; i++) {
            byte[] tmp = new byte[Packet.MAX_DATA];
            int len = (mLen - offset < Packet.MAX_DATA) ? mLen - offset : Packet.MAX_DATA;
            System.arraycopy(message, offset, tmp, 0, len);

            int type;
            if (i == packets.length - 1) {
                type = Packet.END;
                tmp = "".getBytes();
            } else {
                type = Packet.DATA;
            }
            Packet p = new Packet.Builder()
                    .setType(type)
                    .setSequenceNumber(++this.localSeqNum)
                    .setPortNumber(this.targetAddress.getPort())
                    .setPeerAddress(this.targetAddress.getAddress())
                    .setPayload(tmp)
                    .create();
            packets[i] = p;
        }
        return packets;
    }
}