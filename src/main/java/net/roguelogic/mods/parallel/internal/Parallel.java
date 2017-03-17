package net.roguelogic.mods.parallel.internal;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "parallel")
public final class Parallel {

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        // To quote a well known mod.
        // The start of something Great
        new Updater();
    }
}
