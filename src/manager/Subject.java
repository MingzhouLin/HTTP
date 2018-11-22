package manager;

import RUDP.Packet;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract class Subject {
    private List<Manager> list;

    public Subject() {
        this.list = new LinkedList<>();
    }

    public void subscribe(Manager manager) {
        this.list.add(manager);
    }

    //TODO:change the logic of timer
    protected void remove(Manager manager) {
        for (Manager m : list) {
            if (Objects.equals(m, manager)) {
                this.list.remove(manager);
            }
        }
    }

    protected void notifyManager(NoticeMsg msg, Packet packet) {
        for (Manager m : list) {
            try {
                m.update(msg, packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
