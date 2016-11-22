package net.roguelogic.mods.parallel.API;

public class MCThread extends Thread {

    private boolean done=false;

    public boolean getDone(){
        return done;
    }

    /**
     * called every tick to have thread wait for next tick cycle and stay in sync
     */
    protected void waitForTick(){
        done = true;
        try {
            wait();
        } catch (InterruptedException ignored) {}
        done=true;
    }
}
