package net.roguelogic.mods.bigthreads.API;

import net.roguelogic.mods.bigthreads.internal.Management;

public class MCThread extends Thread {
    /**
     * called every tick to have thread wait for next tick cycle and stay in sync
     */
    private void waitForTick(){
        Management.done();
        try {
            wait();
        } catch (InterruptedException ignored) {}
    }
}
