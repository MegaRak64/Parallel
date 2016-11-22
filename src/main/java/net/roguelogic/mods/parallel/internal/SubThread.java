package net.roguelogic.mods.parallel.internal;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.roguelogic.mods.parallel.API.IThreaded;

import java.util.HashSet;

final class SubThread extends Thread {
    private HashSet<IThreaded> toExecute;

    private boolean killed = false;

    private boolean stopped = false;

    private long lastTickTime = -1;

    private boolean done=false;

    public boolean getDone(){
        return done;
    }

    SubThread(HashSet<IThreaded> set) {
        toExecute = set;
        this.setName("Parallel Thread");
    }

    public SubThread begin(){
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
        Management.removeThread(toExecute, this);
    }

    private long tempTime = -1;

    private void waitForTick() {
        done=true;
        while (true)
            try {
                sleep(1000);
            } catch (InterruptedException ignored) {
                break;
            }
        done=false;
    }

    public long getLastTickTime() {
        return lastTickTime;
    }
}
