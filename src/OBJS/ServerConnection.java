package OBJS;

import RUDP.Packet;
import manager.NoticeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;

public class ServerConnection extends Connection {

    private static final Logger logger = LoggerFactory.getLogger(ServerConnection.class);

    private long remoteSeqNum;
    private long localSeqNum;

    private SocketAddress router;
    private ChannelThread channelThread;
    private InetSocketAddress targetAddress;
    private boolean connected;
    private HashMap<Long, Timer> timerMap;

    public ServerConnection(ChannelThread thread, SocketAddress routerAddress) {
        super();
        this.channelThread = thread;
        this.router = routerAddress;
        this.localSeqNum = 1000;
        this.targetAddress = new InetSocketAddress("localhost", 8098);
        this.timerMap = new HashMap<>();
    }

    protected void update(NoticeMsg msg, Packet packet) throws IOException {
        switch (msg) {
            case SYN:
                if (this.remoteSeqNum != packet.getSequenceNumber()) {
                    this.answerSYN(packet);
                    this.answerSYNACK(packet);
                }
                break;
            case SYN_ACK:
                if (packet.getSequenceNumber() == this.localSeqNum + 1) {
                    logger.info("Handshaking #3 SYN packet has received");
                    this.connected = true;
                    if (timerMap.containsKey(this.localSeqNum)) {
                        timerMap.get(this.localSeqNum).setAcked(true);
                    }
                }
                break;
            case TIME_OUT:
                // get current packet's timer, check the status
                long key = packet.getSequenceNumber();
                Timer timer = this.timerMap.get(key);
                if (timer != null && !timer.isAcked()) {
                    // if the real timeout happen
                    this.channelThread.getChannel().send(packet.toBuffer(), this.channelThread.getRouterAddress());
                    new Thread(timer).start();
                    logger.info("Time out happen packet # {}", key);
                } else {
                    // no real timeout, it is time to remove this timer
                    this.timerMap.remove(key);
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
        this.sendSYN(p);
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

    private void sendSYN(Packet p) throws IOException {
        Timer timer = new Timer(p);
        timer.subscribe(this);
        timerMap.put(p.getSequenceNumber(), timer);
        this.channelThread.getChannel().send(p.toBuffer(), this.router);
        new Thread(timer).start();
    }
}
