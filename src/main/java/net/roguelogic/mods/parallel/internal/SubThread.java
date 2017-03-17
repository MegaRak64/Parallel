package net.roguelogic.mods.parallel.internal;

import net.roguelogic.mods.parallel.api.IThreaded;

import java.util.ConcurrentModificationException;
import java.util.HashSet;

final class SubThread extends Thread {
    private IThreaded currentIThreaded = null;
    private HashSet<IThreaded> toExecute;
    private boolean killed = false;
    private long lastTickTime = -1;
    private boolean done = false;
    private long tempTime = -1;

    SubThread(HashSet<IThreaded> set) {
        toExecute = set;
        this.setName("Parallel Thread");
    }

    boolean getDone() {
        return done;
    }

    SubThread begin() {
        super.start();
        return this;
    }

    void update() {
        this.interrupt();
    }

    void kill() {
        killed = true;
    }

    @Override
    public void run() {
        long iThreadedTempTime;
        while (!killed) {
            while (true) {
                waitForTick();
                try {
                    for (IThreaded object : toExecute) {
                        iThreadedTempTime = System.nanoTime();
                        currentIThreaded = object;
                        object.update();
                        Management.setTickTime(object, System.nanoTime() - iThreadedTempTime);
                    }
                    lastTickTime = System.nanoTime() - tempTime;
                    tempTime = System.nanoTime();
                } catch (ConcurrentModificationException ignored) {
                }
            }
        }
        Management.removeThread(toExecute, this);
    }

    private void waitForTick() {
        done = true;
        while (true)
            try {
                sleep(1000);
            } catch (InterruptedException ignored) {
                break;
            }
        done = false;
    }

    long getLastTickTime() {
        return lastTickTime;
    }

    IThreaded getCurrentIThreaded() {
        return currentIThreaded;
    }

    void removeCurrentIThreaded() {
        toExecute.remove(currentIThreaded);
    }
}
