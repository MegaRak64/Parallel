package net.roguelogic.mods.bigthreads.internal;

import net.roguelogic.mods.bigthreads.API.IThreaded;

import java.util.HashSet;

final class SubThread extends Thread {
    private HashSet<IThreaded> toExecute;

    private boolean killed = false;

    private boolean stopped = false;

    private long lastTickTime = -1;

    SubThread(HashSet<IThreaded> set) {
        toExecute = set;
    }

    void update() {
        this.notify();
    }

    void kill() {
        killed = true;
    }

    @Override
    public void run() {
        long iThreadedTempTime;
        while (!killed) {
            while (!stopped) {
                waitForTick();
                for (IThreaded object : toExecute){
                    iThreadedTempTime = System.nanoTime();
                    object.update();
                    Management.setTickTime(object, System.nanoTime() - iThreadedTempTime);
                }
                lastTickTime = System.nanoTime()-tempTime;
                tempTime = System.nanoTime();
            }
        }
        Management.removeThread(this);
    }

    private long tempTime = -1;

    private void waitForTick(){
        Management.done();
        try {
            wait();
        } catch (InterruptedException ignored) {}
    }

    public long getLastTickTime() {
        return lastTickTime;
    }
}
