package net.roguelogic.mods.bigthreads.API;

import net.roguelogic.mods.bigthreads.internal.Management;

public class ParallelThreadRegistry {
    public static void register(IThreaded toRegister){
        Management.register(toRegister);
    }

    public static void unregister(IThreaded toUnregister){
        Management.unregister(toUnregister);
    }
}
