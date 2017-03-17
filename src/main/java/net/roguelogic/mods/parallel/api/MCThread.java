package net.roguelogic.mods.parallel.api;

public class MCThread extends Thread{

    private boolean done=false;

    public boolean getDone(){
        return done;
    }

    /**
     * call every tick to have thread sync with main
     * required to be called as main thread will wait until it is.
     */
    protected void waitForTick(){
        done = true;
        try {
            wait();
        } catch (InterruptedException ignored) {}
        done=true;
    }

    public void update() {
        this.interrupt();
    }
}
