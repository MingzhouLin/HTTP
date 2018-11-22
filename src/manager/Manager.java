package manager;

import RUDP.Packet;

import java.io.IOException;

public abstract class Manager {
    protected abstract void update(NoticeMsg msg, Packet packet) throws IOException;
}
