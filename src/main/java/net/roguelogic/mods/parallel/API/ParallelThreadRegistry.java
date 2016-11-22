package net.roguelogic.mods.parallel.API;

import net.roguelogic.mods.parallel.internal.Management;

public class ParallelThreadRegistry {
    public static void register(IThreaded toRegister){
        Management.register(toRegister);
    }

    public static void unregister(IThreaded toUnregister){
        Management.unregister(toUnregister);
    }
}