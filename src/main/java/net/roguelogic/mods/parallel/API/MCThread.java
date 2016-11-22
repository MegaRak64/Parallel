package net.roguelogic.mods.parallel.API;

import net.roguelogic.mods.parallel.internal.Management;

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
