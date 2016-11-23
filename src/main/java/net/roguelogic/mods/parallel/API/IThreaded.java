package net.roguelogic.mods.parallel.api;

public interface IThreaded {
    /**
     * called from a synchronised thread
     */
    void update();
}
