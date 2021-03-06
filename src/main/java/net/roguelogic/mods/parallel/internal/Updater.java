package net.roguelogic.mods.parallel.internal;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

final class Updater {
    private boolean firstTick = true;

    Updater() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldTick(final TickEvent.WorldTickEvent event) {

        if (TickEvent.Phase.START == event.phase && Thread.currentThread().getName().equals("Server thread")) {
            if (firstTick) {
                firstTick = false;
                Management.init();
            }
            Management.update();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onWorldUnload(final WorldEvent.Unload unloadWorldEvent) {
        Management.worldUnload();
    }
}
