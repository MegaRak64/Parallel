package net.roguelogic.mods.parallel.API;

public interface IThreaded {
    /**
     * called from a synchronised thread
     */
    void update();
}
