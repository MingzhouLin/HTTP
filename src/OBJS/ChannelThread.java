package OBJS;

import RUDP.Packet;
import manager.NoticeMsg;
import manager.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;

public class ChannelThread extends Subject implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ChannelThread.class);
    private DatagramChannel channel;
    private SocketAddress routerAddress;
    private SendBuffer sendBuffer;
    private Selector selector;
    private Connection connection;

    private final List<Packet> queue;

    public ChannelThread(DatagramChannel channel, SocketAddress routerAddress) {
        this.channel = channel;
        this.routerAddress = routerAddress;
        this.queue = new LinkedList<>();
        this.sendBuffer = new SendBuffer(queue, this);
        this.subscribe(sendBuffer);
    }

    @Override
    public void run() {
        try {
            this.channel.configureBlocking(false);
            this.selector = Selector.open();
            this.channel.register(this.selector, OP_READ);
            while (true) {

                if (this.connection != null && this.connection.isConnected()
                        && this.sendBuffer.getHasSth2Send() > 0) {
                    SelectionKey key = this.channel.keyFor(this.selector);
                    key.interestOps(SelectionKey.OP_WRITE);
                }

                this.selector.select(500);
                Set<SelectionKey> keys = selector.selectedKeys();
                if (!keys.isEmpty()) {
                    for (SelectionKey k : keys) {
                        if (k.isReadable()) {
                            this.read(k);
                        } else if (k.isWritable()) {
                            this.write(k);
                        }
                    }
                    keys.clear();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(SelectionKey k) throws IOException {
        DatagramChannel channel = (DatagramChannel) k.channel();
        synchronized (queue) {
            int i = 0;
            int time = this.sendBuffer.getHasSth2Send();
            while (time > 0) {
                Packet p = this.queue.get(i);
                channel.send(p.toBuffer(), this.routerAddress);
                // start the timer thread
                new Thread(this.sendBuffer.getTimerMap().get(p.getSequenceNumber())).start();
                time--;
                i++;
            }
            this.sendBuffer.setHasSth2Send(time);
        }
        k.interestOps(OP_READ);
    }

    public void read(SelectionKey k) throws IOException {
        DatagramChannel channel = (DatagramChannel) k.channel();
        ByteBuffer buffer = ByteBuffer.allocate(Packet.MAX_LEN);
        channel.receive(buffer);
        buffer.flip();
        Packet recv = Packet.fromBuffer(buffer);

        handler(recv);
    }

    private void handler(Packet packet) {
        int type = packet.getType();
        switch (type) {
            // ** if the packet is a handshaking packet, tell the connection
            case Packet.SYN_1:
                this.notifyManager(NoticeMsg.SYN, packet);
                break;
            case Packet.SYN_2:
                this.notifyManager(NoticeMsg.SYN_ACK, packet);
                break;
            // ** end of handshaking packet handler

            // if the packet is a data ack, tell the sendBuffer
            case Packet.ACK:
                this.notifyManager(NoticeMsg.ACK, packet);
                break;

            // if the packet is the data, tell the receiver buffer
            case Packet.DATA:
                this.notifyManager(NoticeMsg.DATA, packet);
                break;
            // if the packet is the end of data indicator, tell the receiver buffer
            case Packet.END:
                this.notifyManager(NoticeMsg.END_OF_DATA, packet);
                break;
        }
    }

    public void send(byte[] message) throws IOException {
        // break the message into chunks
        Packet[] packets = this.connection.makeChunks(message);
        this.send(packets);
    }

//    public void send(Packet packet) {
//        synchronized (this.queue) {
//            this.queue.add(packet);
//            logger.info("Add packet #{} to the queue", packet.getSequenceNumber());
//            this.notifyManager(NoticeMsg.WIN_CHECK, packet);
//        }
//    }

    private void send(Packet[] packets) {
        synchronized (this.queue) {
            for (Packet p : packets) {
                this.queue.add(p);
                logger.info("Add packet #{} to the queue", p.getSequenceNumber());
                this.notifyManager(NoticeMsg.WIN_CHECK, p);
            }
        }
    }

    public DatagramChannel getChannel() {
        return channel;
    }

    public SocketAddress getRouterAddress() {
        return routerAddress;
    }

    public void bind(Connection connection) {
        this.connection = connection;
    }

}
