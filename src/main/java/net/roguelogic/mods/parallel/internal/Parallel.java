package net.roguelogic.mods.parallel.internal;

import net.minecraftforge.fml.common.Mod;

@Mod(modid = "parallel")
public final class Parallel {

    @Mod.EventHandler
    public void onPreInit(){
        // To quote a well known mod.
        // The start of something Great
        Management.init();
        new Updater();
    }
}
