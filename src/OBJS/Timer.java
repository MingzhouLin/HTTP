package OBJS;

import RUDP.Packet;
import manager.NoticeMsg;
import manager.Subject;

public class Timer extends Subject implements Runnable {
    private final long TIME_OUT = 1000;

    private Packet packet;
    private boolean isAcked;

    public Timer(Packet packet) {
        this.packet   = packet;
        this.isAcked = false;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(TIME_OUT);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // the timer wait for the interval time and time out it
        this.notifyManager(NoticeMsg.TIME_OUT, packet);
    }

    public boolean isAcked() {
        return isAcked;
    }

    public void setAcked(boolean acked) {
        this.isAcked = acked;
    }
}
