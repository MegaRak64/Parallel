package net.roguelogic.mods.bigthreads.API;

public interface IThreaded {
    /**
     * called from a synchronised thread
     */
    void update();
}
