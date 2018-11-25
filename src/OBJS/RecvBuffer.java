package OBJS;

import RUDP.Packet;
import manager.Manager;
import manager.NoticeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.*;

public class RecvBuffer extends Manager {

    private static final Logger logger = LoggerFactory.getLogger(RecvBuffer.class);

    private final int WIN_SIZE = 4;
    private final List<Packet> buffer;

    private boolean isLastPacket;
    private long curMinNum;
    private final LinkedList<Long> endSeqNums;
    private final LinkedList<Long> historyEnd;
    private Packet[] window;
    private ChannelThread thread;
    private SocketAddress rounter;
    private Connection connection;

    public RecvBuffer(ChannelThread thread, SocketAddress rounter, Connection connection) {
        this.thread = thread;
        this.buffer = new LinkedList<>();
        this.window = new Packet[this.WIN_SIZE];
        this.rounter = rounter;
        this.connection = connection;
        this.curMinNum = -2;
        this.endSeqNums = new LinkedList<>();
        this.historyEnd = new LinkedList<>();
    }


    @Override
    protected void update(NoticeMsg msg, Packet packet) throws IOException {
        switch (msg) {
            case END_OF_DATA:
                long endSeqNum = packet.getSequenceNumber();
                if (!endSeqNums.contains(endSeqNum) && !historyEnd.contains(endSeqNum)) {
                    synchronized (endSeqNums) {
                        this.endSeqNums.add(endSeqNum);
                        this.historyEnd.add(endSeqNum);
                    }
                }
                Collections.sort(endSeqNums);
                logger.info("Receive an end of data packet, endSeqNum = # {}", endSeqNum);
            case DATA:
                logger.debug("RecvBuffer receive a packet, handling ...");
                Packet p = this.handleDataPacket(packet);
                SocketAddress addr = new InetSocketAddress(p.getPeerAddress(), p.getPeerPort());
                // ACK do not need to go through the thread since it doesn't need a timer
                this.thread.getChannel().send(p.toBuffer(), this.rounter);
                logger.info("An ACK for packet #{} has been sent", packet.getSequenceNumber());
                break;
            case SYN:
                long initialSeqNum = packet.getSequenceNumber() + 1;
                logger.info("RecvBuffer receive the initial seqNum #{}", initialSeqNum);
                this.curMinNum = initialSeqNum;
                break;
        }
    }

    public synchronized int receive(ByteBuffer result) {
//        if (this.endSeqNum == this.curMinNum) return -1;
        int len = 0;
        long endSeqNum = endSeqNums.isEmpty() ? -1 : endSeqNums.getFirst();

        // when the user want to receive something, if the
        // buffer is empty, block the user thread here
        while (endSeqNum > this.curMinNum - 1 || (endSeqNum == -1)) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            endSeqNum = endSeqNums.isEmpty() ? -1 : endSeqNums.getFirst();
        }
        synchronized (this.buffer) {
            if (!this.buffer.isEmpty()) {
                int i = 0;
                List<Byte> byteList = new LinkedList<>();
                List<Packet> removeBuffer = new LinkedList<>();
                for (Packet p : this.buffer) {
                    if (p.getSequenceNumber() <= endSeqNum) {
                        for (byte b :
                                p.getPayload()) {
                            if (b == 0) break;
                            byteList.add(b);
                        }
                        removeBuffer.add(p);
//                        this.windowClean(p);
                    }
                }
                for (Packet p :
                        removeBuffer) {
                    this.buffer.remove(p);
                }
                byte[] tmp = new byte[byteList.size()];
                for (byte b :
                        byteList) {
                    tmp[i++] = b;
                }
                len = tmp.length;
                result.clear();
                result.put(tmp);
                endSeqNums.removeFirst();
            }
        }

        return len;
    }

    private void windowClean(Packet p) {
        for (int i = 0; i < this.window.length; i++) {
            if (window[i] != null && window[i].getSequenceNumber() == p.getSequenceNumber()) {
                window[i] = null;
            }
        }
    }

    private Packet handleDataPacket(Packet p) {
        long seqNum = p.getSequenceNumber();
        if (seqNum >= this.curMinNum && seqNum < this.curMinNum + this.WIN_SIZE) {
            // if the sequence number indicate that the packet is the one we
            // are waiting for, put into the buffer and if we have the check
            // the contiguous packets can give to the secondary buffer
            int idx = (int) (seqNum - this.curMinNum);
            this.window[idx] = p;
            logger.info("packet #{} is put into window", this.window[idx].getSequenceNumber());
            this.checkAndMove();
        } else {
            logger.debug("Already received it or not expecting packet # {}", seqNum);
        }
        // generate an ACK for that packet and send it back to the sender
        // no matter the packet is the one we are waiting for or not
//        synchronized (this.buffer) {
//            this.buffer.notify();
//        }
        return constructAckPacket(p);
    }

    private Packet constructAckPacket(Packet p) {
        logger.debug("Generate a ACK for packet #{}", p.getSequenceNumber());
        return new Packet.Builder()
                .setType(Packet.ACK)
                .setSequenceNumber((p.getSequenceNumber()))
                .setPortNumber(p.getPeerPort())
                .setPeerAddress(p.getPeerAddress())
                .setPayload("".getBytes())
                .create();
    }

    private void checkAndMove() {
        int idx;
        boolean flag;
        do {
            flag = false;
            idx = 0;
            for (; idx < this.WIN_SIZE; idx++) {
                // if the idx packet is null means not continuous
                if (this.window[idx] == null) {
                    break;
                } else {
                    // otherwise, add the payload to the buffer
//                for (byte b : this.window[idx].getPayload()) {
//                    if (b == 0) break;
//                    this.buffer.add(b);
//                }
                    synchronized (this.buffer) {
                        this.buffer.add(this.window[idx]);
                        logger.info("packet #{} is put into buffer", this.window[idx].getSequenceNumber());
                        this.window[idx] = null;
                    }
                    this.curMinNum += 1;
                    flag = true;
                }
            }
        } while (flag && this.updateWindow());
    }

    private boolean updateWindow() {
        for (int i = 0; i < this.WIN_SIZE; i++) {
            if (this.window[i] != null) {
                this.window[(int) (this.window[i].getSequenceNumber() - this.curMinNum)] = this.window[i];
                this.window[i] = null;
            }
        }
        return true;
    }
}